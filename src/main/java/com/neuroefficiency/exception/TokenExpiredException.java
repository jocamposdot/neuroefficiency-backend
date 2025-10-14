package com.neuroefficiency.exception;

/**
 * Exception lançada quando um token de reset de senha expirou
 * 
 * Tokens expiram após 30 minutos da criação.
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
public class TokenExpiredException extends RuntimeException {
    
    public TokenExpiredException() {
        super("Token de reset de senha expirou. Solicite um novo token.");
    }

    public TokenExpiredException(String message) {
        super(message);
    }
}

