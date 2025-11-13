package com.neuroefficiency.service;

import com.neuroefficiency.domain.enums.AuditEventType;
import com.neuroefficiency.domain.model.PasswordResetAudit;
import com.neuroefficiency.domain.model.PasswordResetToken;
import com.neuroefficiency.domain.model.Usuario;
import com.neuroefficiency.domain.repository.PasswordResetAuditRepository;
import com.neuroefficiency.domain.repository.PasswordResetTokenRepository;
import com.neuroefficiency.domain.repository.UsuarioRepository;
import com.neuroefficiency.dto.request.PasswordResetConfirmDto;
import com.neuroefficiency.dto.request.PasswordResetRequestDto;
import com.neuroefficiency.exception.PasswordMismatchException;
import com.neuroefficiency.exception.RateLimitExceededException;
import com.neuroefficiency.exception.TokenExpiredException;
import com.neuroefficiency.exception.TokenInvalidException;
import com.neuroefficiency.util.TokenUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Serviço de recuperação de senha
 * 
 * Responsabilidades:
 * - Geração e validação de tokens
 * - Rate limiting (3 tentativas/hora)
 * - Auditoria (compliance LGPD)
 * - Anti-enumeração (resposta padronizada)
 * - Envio de emails
 * - Limpeza de tokens expirados
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetAuditRepository auditRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRY_MINUTES = 30;
    private static final int RATE_LIMIT_ATTEMPTS = 3;
    private static final int RATE_LIMIT_HOURS = 1;

    /**
     * Solicita reset de senha (envia token por email)
     * 
     * IMPORTANTE: Resposta sempre positiva para prevenir enumeração de usuários.
     * 
     * @param request dados da solicitação (email)
     * @param httpRequest request HTTP para capturar IP e User-Agent
     * @param locale idioma para o email
     */
    @Transactional
    public void requestPasswordReset(
            PasswordResetRequestDto request,
            HttpServletRequest httpRequest,
            Locale locale) {

        String email = request.getEmail().toLowerCase().trim();
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        log.info("Solicitação de reset de senha para email: {}", sanitizeEmail(email));

        try {
            // 1. Verificar rate limiting
            checkRateLimit(email, ipAddress);

            // 2. Buscar usuário por email (pode não existir - anti-enumeração)
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);

            if (usuario != null) {
                // 3. Invalidar tokens antigos do usuário
                tokenRepository.invalidateAllByUsuarioId(usuario.getId(), LocalDateTime.now());

                // 4. Gerar novo token
                String rawToken = TokenUtils.generateSecureToken();
                String tokenHash = TokenUtils.hashToken(rawToken);

                // 5. Salvar token no banco
                PasswordResetToken token = PasswordResetToken.builder()
                        .tokenHash(tokenHash)
                        .usuario(usuario)
                        .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))
                        .build();
                tokenRepository.save(token);

                // 6. Enviar email com token
                try {
                    emailService.sendPasswordResetEmail(email, rawToken, locale);
                    
                    // Auditoria: sucesso
                    logAudit(email, ipAddress, userAgent, AuditEventType.AUTH_PASSWORD_RESET_REQUEST, true, null);
                    
                    log.info("Token de reset gerado e enviado com sucesso para: {}", sanitizeEmail(email));
                    
                } catch (MessagingException e) {
                    log.error("Erro ao enviar email para: {}", sanitizeEmail(email), e);
                    logAudit(email, ipAddress, userAgent, AuditEventType.AUTH_PASSWORD_RESET_REQUEST, false, "Erro ao enviar email");
                    throw new RuntimeException("Erro ao enviar email. Tente novamente mais tarde.");
                }
            } else {
                // Email não existe, mas não revelamos isso (anti-enumeração)
                log.warn("Tentativa de reset para email não cadastrado: {}", sanitizeEmail(email));
                logAudit(email, ipAddress, userAgent, AuditEventType.AUTH_PASSWORD_RESET_REQUEST, true, "Email não encontrado (oculto do usuário)");
                
                // Delay artificial para parecer processamento real (anti-timing-attack)
                simulateDelay();
            }

        } catch (RateLimitExceededException e) {
            logAudit(email, ipAddress, userAgent, AuditEventType.SECURITY_RATE_LIMIT_EXCEEDED, false, e.getMessage());
            throw e;
        }

        // IMPORTANTE: Sempre retorna mensagem de sucesso (anti-enumeração)
        // Não revelamos se o email existe ou não
    }

    /**
     * Confirma reset de senha (valida token e troca senha)
     * 
     * @param request dados da confirmação (token, senha nova)
     * @param httpRequest request HTTP para capturar IP e User-Agent
     * @param locale idioma para email de confirmação
     */
    @Transactional
    public void confirmPasswordReset(
            PasswordResetConfirmDto request,
            HttpServletRequest httpRequest,
            Locale locale) {

        String rawToken = request.getToken();
        String tokenHash = TokenUtils.hashToken(rawToken);
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        log.info("Tentativa de confirmação de reset de senha com token");

        // 1. Validar senhas coincidem
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        // 2. Buscar token
        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new TokenInvalidException());

        String email = token.getUsuario().getEmail();

        // 3. Validar token
        if (token.isUsed()) {
            logAudit(email, ipAddress, userAgent, AuditEventType.SECURITY_INVALID_TOKEN, false, "Token já foi usado");
            throw new TokenInvalidException("Token já foi usado");
        }

        if (token.isExpired()) {
            logAudit(email, ipAddress, userAgent, AuditEventType.SECURITY_INVALID_TOKEN, false, "Token expirado");
            throw new TokenExpiredException();
        }

        // 4. Atualizar senha do usuário
        Usuario usuario = token.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);

        // 5. Marcar token como usado
        token.markAsUsed();
        tokenRepository.save(token);

        // 6. Invalidar todos os outros tokens do usuário
        tokenRepository.invalidateAllByUsuarioId(usuario.getId(), LocalDateTime.now());

        // 7. Enviar email de confirmação
        try {
            emailService.sendPasswordChangedEmail(email, LocalDateTime.now(), locale);
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de confirmação para: {}", sanitizeEmail(email), e);
            // Não falhamos a operação por causa do email de confirmação
        }

        // 8. Auditoria: sucesso
        logAudit(email, ipAddress, userAgent, AuditEventType.AUTH_PASSWORD_RESET_CONFIRM, true, null);

        log.info("Senha alterada com sucesso para usuário: {}", usuario.getId());
    }

    /**
     * Valida se um token é válido (sem usá-lo)
     * 
     * @param token token a ser validado
     * @return true se válido, false caso contrário
     */
    public boolean validateToken(String token) {
        try {
            String tokenHash = TokenUtils.hashToken(token);
            PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
                    .orElse(null);

            return resetToken != null && resetToken.isValid();
        } catch (Exception e) {
            log.error("Erro ao validar token", e);
            return false;
        }
    }

    /**
     * Verifica rate limiting (3 tentativas por hora por email/IP)
     * 
     * @param email email a verificar
     * @param ipAddress IP a verificar
     * @throws RateLimitExceededException se excedeu o limite
     */
    private void checkRateLimit(String email, String ipAddress) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(RATE_LIMIT_HOURS);

        long attemptsByEmail = auditRepository.countByEmailAndTimestampAfter(email, oneHourAgo);
        long attemptsByIP = auditRepository.countByIpAddressAndTimestampAfter(ipAddress, oneHourAgo);

        if (attemptsByEmail >= RATE_LIMIT_ATTEMPTS) {
            log.warn("Rate limit excedido para email: {}", sanitizeEmail(email));
            throw new RateLimitExceededException(
                String.format("Limite de %d tentativas por hora excedido. Tente novamente mais tarde.", RATE_LIMIT_ATTEMPTS)
            );
        }

        if (attemptsByIP >= RATE_LIMIT_ATTEMPTS) {
            log.warn("Rate limit excedido para IP: {}", ipAddress);
            throw new RateLimitExceededException(
                String.format("Limite de %d tentativas por hora excedido. Tente novamente mais tarde.", RATE_LIMIT_ATTEMPTS)
            );
        }
    }

    /**
     * Loga evento na tabela de auditoria
     */
    private void logAudit(String email, String ipAddress, String userAgent,
                         AuditEventType eventType, boolean success, String errorMessage) {
        PasswordResetAudit audit = PasswordResetAudit.builder()
                .email(email)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .eventType(eventType)
                .success(success)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();

        auditRepository.save(audit);
    }

    /**
     * Extrai IP do cliente (considerando proxies)
     */
    private String getClientIP(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Delay artificial para prevenir timing attacks
     */
    private void simulateDelay() {
        try {
            Thread.sleep(500 + (long) (Math.random() * 500)); // 500-1000ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sanitiza email para logs
     */
    private String sanitizeEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        if (parts[0].isEmpty()) {
            return "***@" + parts[1];
        }
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    /**
     * Job agendado: Limpeza de tokens expirados ou usados
     * 
     * Executado diariamente às 3h da manhã.
     */
    @Scheduled(cron = "0 0 3 * * *")  // Todos os dias às 3h
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Iniciando limpeza de tokens expirados/usados");
        
        int deleted = tokenRepository.deleteExpiredOrUsed(LocalDateTime.now());
        
        log.info("Limpeza concluída: {} tokens removidos", deleted);
    }
}

