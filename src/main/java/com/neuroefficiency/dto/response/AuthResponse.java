package com.neuroefficiency.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de autenticação
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String message;
    private UserResponse user;
    private String sessionId;

    public static AuthResponse success(String message, UserResponse user) {
        return AuthResponse.builder()
                .message(message)
                .user(user)
                .build();
    }

    public static AuthResponse success(String message) {
        return AuthResponse.builder()
                .message(message)
                .build();
    }
}

