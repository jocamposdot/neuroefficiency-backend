package com.neuroefficiency.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler global de exceções
 * 
 * Captura e trata exceções lançadas pelos controllers.
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Tratamento para erros de validação
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, Object> errors = new HashMap<>();
        errors.put("timestamp", LocalDateTime.now());
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Validation Failed");
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errors.put("fieldErrors", fieldErrors);
        
        log.warn("Erro de validação: {}", fieldErrors);
        
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Tratamento para username já existente
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException ex) {
        
        Map<String, Object> error = buildErrorResponse(
            HttpStatus.CONFLICT,
            "Username já existe",
            ex.getMessage()
        );
        
        log.warn("Tentativa de registro com username duplicado");
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Tratamento para senhas não coincidentes
     */
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordMismatch(
            PasswordMismatchException ex) {
        
        Map<String, Object> error = buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Senhas não coincidem",
            ex.getMessage()
        );
        
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Tratamento para credenciais inválidas
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex) {
        
        Map<String, Object> error = buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Credenciais inválidas",
            "Username ou password incorretos"
        );
        
        log.warn("Tentativa de login com credenciais inválidas");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Tratamento para usuário não encontrado
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(
            UsernameNotFoundException ex) {
        
        Map<String, Object> error = buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Usuário não encontrado",
            "Username ou password incorretos"
        );
        
        log.warn("Tentativa de login com usuário inexistente");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Tratamento genérico de exceções
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex) {
        
        Map<String, Object> error = buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro interno do servidor",
            "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde."
        );
        
        log.error("Erro inesperado: ", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Método auxiliar para construir resposta de erro
     */
    private Map<String, Object> buildErrorResponse(
            HttpStatus status, String error, String message) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        
        return response;
    }
}

