package com.neuroefficiency.exception;

/**
 * Exceção lançada quando uma permissão já existe no sistema
 * 
 * @author Joao Fuhrmann
 * @version 1.0 - Fase 3 RBAC
 * @since 2025-10-16
 */
public class PermissionAlreadyExistsException extends RuntimeException {

    public PermissionAlreadyExistsException(String message) {
        super(message);
    }

    public PermissionAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
