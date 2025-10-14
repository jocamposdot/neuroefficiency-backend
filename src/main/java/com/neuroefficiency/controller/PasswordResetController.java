package com.neuroefficiency.controller;

import com.neuroefficiency.dto.request.PasswordResetConfirmDto;
import com.neuroefficiency.dto.request.PasswordResetRequestDto;
import com.neuroefficiency.dto.response.ApiResponse;
import com.neuroefficiency.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

/**
 * Controller para recuperação de senha
 * 
 * Endpoints:
 * - POST /api/auth/password-reset/request - Solicitar token de reset
 * - POST /api/auth/password-reset/confirm - Confirmar reset com token
 * - GET /api/auth/password-reset/validate-token/{token} - Validar token
 * 
 * IMPORTANTE: Usa formato ApiResponse<T> (diferente dos endpoints antigos)
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Endpoint: Solicitar reset de senha
     * 
     * POST /api/auth/password-reset/request
     * 
     * Request body:
     * {
     *   "email": "user@example.com"
     * }
     * 
     * Response (sempre 200 OK - anti-enumeração):
     * {
     *   "success": true,
     *   "data": null,
     *   "message": "Se o email existir, você receberá instruções para redefinir sua senha."
     * }
     * 
     * Rate limiting: 3 tentativas/hora por email ou IP
     * 
     * @param request dados da solicitação
     * @param httpRequest request HTTP para capturar IP/User-Agent
     * @param locale idioma do email (Accept-Language header)
     * @return resposta padronizada
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDto request,
            HttpServletRequest httpRequest,
            @RequestHeader(value = "Accept-Language", defaultValue = "pt-BR") Locale locale) {

        log.info("Recebida solicitação de reset de senha");

        passwordResetService.requestPasswordReset(request, httpRequest, locale);

        // IMPORTANTE: Resposta sempre igual (anti-enumeração)
        // Não revelamos se o email existe ou não
        return ResponseEntity.ok(
            ApiResponse.success(
                "Se o email existir, você receberá instruções para redefinir sua senha."
            )
        );
    }

    /**
     * Endpoint: Confirmar reset de senha
     * 
     * POST /api/auth/password-reset/confirm
     * 
     * Request body:
     * {
     *   "token": "abc123...64chars",
     *   "newPassword": "NewPassword123!",
     *   "confirmPassword": "NewPassword123!"
     * }
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "data": null,
     *   "message": "Senha redefinida com sucesso!"
     * }
     * 
     * Possíveis erros:
     * - 400 BAD_REQUEST: Token inválido, senhas não coincidem
     * - 410 GONE: Token expirado
     * 
     * @param request dados da confirmação
     * @param httpRequest request HTTP para auditoria
     * @param locale idioma do email de confirmação
     * @return resposta padronizada
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmDto request,
            HttpServletRequest httpRequest,
            @RequestHeader(value = "Accept-Language", defaultValue = "pt-BR") Locale locale) {

        log.info("Recebida confirmação de reset de senha");

        passwordResetService.confirmPasswordReset(request, httpRequest, locale);

        return ResponseEntity.ok(
            ApiResponse.success(
                "Senha redefinida com sucesso! Você já pode fazer login com sua nova senha."
            )
        );
    }

    /**
     * Endpoint: Validar token (sem usá-lo)
     * 
     * GET /api/auth/password-reset/validate-token/{token}
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "data": {
     *     "valid": true
     *   },
     *   "message": "Token válido"
     * }
     * 
     * Response (token inválido):
     * {
     *   "success": true,
     *   "data": {
     *     "valid": false
     *   },
     *   "message": "Token inválido ou expirado"
     * }
     * 
     * @param token token a ser validado
     * @return resposta com status de validade
     */
    @GetMapping("/validate-token/{token}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> validateToken(
            @PathVariable String token) {

        log.info("Recebida validação de token");

        boolean isValid = passwordResetService.validateToken(token);

        if (isValid) {
            return ResponseEntity.ok(
                ApiResponse.success(
                    Map.of("valid", true),
                    "Token válido"
                )
            );
        } else {
            return ResponseEntity.ok(
                ApiResponse.success(
                    Map.of("valid", false),
                    "Token inválido ou expirado"
                )
            );
        }
    }

    /**
     * Health check do serviço de reset de senha
     * 
     * GET /api/auth/password-reset/health
     * 
     * @return status 200 OK
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(
            ApiResponse.success(
                Map.of(
                    "status", "UP",
                    "service", "password-reset",
                    "version", "1.0"
                ),
                "Serviço de recuperação de senha operacional"
            )
        );
    }
}

