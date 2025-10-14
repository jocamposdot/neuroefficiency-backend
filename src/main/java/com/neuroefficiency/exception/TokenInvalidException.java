package com.neuroefficiency.exception;

/**
 * Exception lançada quando um token de reset de senha é inválido
 * 
 * Causas possíveis:
 * - Token não existe no banco
 * - Token já foi usado
 * - Token malformado
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
public class TokenInvalidException extends RuntimeException {
    
    public TokenInvalidException() {
        super("Token de reset de senha inválido ou já foi usado.");
    }

    public TokenInvalidException(String message) {
        super(message);
    }
}

