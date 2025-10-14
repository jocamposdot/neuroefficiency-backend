package com.neuroefficiency.exception;

/**
 * Exception lançada quando o rate limit é excedido
 * 
 * Rate limits:
 * - 3 tentativas por hora por email
 * - 3 tentativas por hora por IP
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
public class RateLimitExceededException extends RuntimeException {
    
    public RateLimitExceededException() {
        super("Limite de tentativas excedido. Tente novamente em 1 hora.");
    }

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}

