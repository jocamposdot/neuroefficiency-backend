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
 * @version 2.0 - Adicionado campo email (Tarefa 2)
 * @since 2025-10-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;  // TAREFA 2: Email para recuperação de senha
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
                .email(usuario.getEmail())  // TAREFA 2
                .enabled(usuario.getEnabled())
                .createdAt(usuario.getCreatedAt())
                .updatedAt(usuario.getUpdatedAt())
                .build();
    }
}

