package com.neuroefficiency.dto.response;

import com.neuroefficiency.domain.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta contendo dados do usuário
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converte uma entidade Usuario para UserResponse
     */
    public static UserResponse from(Usuario usuario) {
        return UserResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .enabled(usuario.getEnabled())
                .createdAt(usuario.getCreatedAt())
                .updatedAt(usuario.getUpdatedAt())
                .build();
    }
}

