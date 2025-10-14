package com.neuroefficiency.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Serviço de envio de emails
 * 
 * Características:
 * - Emails multipart (HTML + texto simples)
 * - Internacionalização (i18n) via Accept-Language
 * - Templates Thymeleaf
 * - Suporte pt-BR e en-US
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Envia email de reset de senha
     * 
     * @param to email do destinatário
     * @param token token de reset (64 chars hex)
     * @param locale idioma do email (pt-BR ou en-US)
     * @throws MessagingException se houver erro no envio
     */
    public void sendPasswordResetEmail(String to, String token, Locale locale) throws MessagingException {
        log.info("Preparando email de reset de senha para: {}", sanitizeEmail(to));

        // Construir link de reset
        String resetLink = String.format("%s/#/reset-password?token=%s", frontendUrl, token);

        // Preparar contexto para template
        Context context = new Context(locale);
        context.setVariable("resetLink", resetLink);
        context.setVariable("supportUrl", frontendUrl + "/#/help");

        // Obter assunto internacionalizado
        String subject = messageSource.getMessage(
            "email.password.reset.subject",
            null,
            locale
        );

        // Gerar HTML e texto
        String htmlContent = templateEngine.process("email/password-reset", context);
        String textContent = templateEngine.process("email/password-reset.txt", context);

        // Enviar email multipart
        sendMultipartEmail(to, subject, htmlContent, textContent);

        log.info("Email de reset enviado com sucesso para: {}", sanitizeEmail(to));
    }

    /**
     * Envia email de confirmação de senha alterada
     * 
     * @param to email do destinatário
     * @param timestamp data/hora da alteração
     * @param locale idioma do email (pt-BR ou en-US)
     * @throws MessagingException se houver erro no envio
     */
    public void sendPasswordChangedEmail(String to, LocalDateTime timestamp, Locale locale) throws MessagingException {
        log.info("Preparando email de confirmação de senha alterada para: {}", sanitizeEmail(to));

        // Preparar contexto para template
        Context context = new Context(locale);
        context.setVariable("timestamp", timestamp);
        context.setVariable("supportUrl", frontendUrl + "/#/help");

        // Obter assunto internacionalizado
        String subject = messageSource.getMessage(
            "email.password.changed.subject",
            null,
            locale
        );

        // Gerar HTML e texto
        String htmlContent = templateEngine.process("email/password-changed", context);
        String textContent = templateEngine.process("email/password-changed.txt", context);

        // Enviar email multipart
        sendMultipartEmail(to, subject, htmlContent, textContent);

        log.info("Email de confirmação enviado com sucesso para: {}", sanitizeEmail(to));
    }

    /**
     * Envia email multipart (HTML + texto simples)
     * 
     * @param to destinatário
     * @param subject assunto
     * @param htmlContent conteúdo HTML
     * @param textContent conteúdo texto simples
     * @throws MessagingException se houver erro no envio
     */
    private void sendMultipartEmail(String to, String subject, String htmlContent, String textContent) 
            throws MessagingException {
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        
        // Multipart: texto simples como fallback, HTML como principal
        helper.setText(textContent, htmlContent);

        mailSender.send(message);
    }

    /**
     * Sanitiza email para logs (previne exposição completa)
     * 
     * Exemplo: user@example.com -> u***@example.com
     * 
     * @param email email a ser sanitizado
     * @return email sanitizado
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
}

