package com.neuroefficiency.exception;

/**
 * Exception lançada quando as senhas não coincidem durante o registro
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
public class PasswordMismatchException extends RuntimeException {

    public PasswordMismatchException(String message) {
        super(message);
    }

    public PasswordMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}

