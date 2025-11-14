package com.neuroefficiency.exception;

/**
 * Exception lançada quando se tenta criar um admin via setup
 * mas já existe pelo menos um admin no sistema.
 */
public class AdminAlreadyExistsException extends RuntimeException {
    
    public AdminAlreadyExistsException(String message) {
        super(message);
    }
}

