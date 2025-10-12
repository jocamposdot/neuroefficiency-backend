package com.neuroefficiency.exception;

/**
 * Exception lançada quando se tenta registrar um username que já existe
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    public UsernameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

