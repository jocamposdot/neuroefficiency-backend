package com.neuroefficiency.domain.enums;

/**
 * Tipos de eventos de auditoria de reset de senha
 * 
 * Usado para rastrear todas as tentativas de recuperação de senha
 * para compliance LGPD, análise de segurança e rate limiting.
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
public enum AuditEventType {
    
    /**
     * Solicitação de reset de senha (POST /password-reset/request)
     * Logado sempre, independente se email existe ou não
     */
    REQUEST,
    
    /**
     * Reset de senha bem-sucedido (POST /password-reset/confirm)
     * Token válido, senha redefinida com sucesso
     */
    SUCCESS,
    
    /**
     * Falha no reset de senha
     * Token inválido, senhas não coincidem, etc.
     */
    FAILURE,
    
    /**
     * Tentativa de uso de token expirado
     * Token encontrado mas expirado (> 30min)
     */
    EXPIRED_TOKEN,
    
    /**
     * Tentativa de uso de token inválido
     * Token não encontrado ou já usado
     */
    INVALID_TOKEN,
    
    /**
     * Bloqueio por rate limit
     * Mais de 3 tentativas por hora (por email ou IP)
     */
    RATE_LIMIT
}

