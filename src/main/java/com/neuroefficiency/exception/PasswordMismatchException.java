package com.neuroefficiency.exception;

/**
 * Exception lançada quando as senhas não coincidem
 * 
 * Usado quando:
 * - newPassword != confirmPassword no reset de senha
 * - newPassword != confirmPassword no registro
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
public class PasswordMismatchException extends RuntimeException {
    
    public PasswordMismatchException() {
        super("As senhas não coincidem.");
    }

    public PasswordMismatchException(String message) {
        super(message);
    }
}
