package com.neuroefficiency.dto.response;

import com.neuroefficiency.domain.model.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO de resposta para Permission
 * Evita problemas de lazy loading e serialização circular
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {
    private Long id;
    private String name;
    private String description;
    private String resource;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> roles; // Apenas os nomes das roles

    /**
     * Converte Permission entity para PermissionResponse DTO
     */
    public static PermissionResponse fromEntity(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .active(permission.getActive())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .roles(null) // Por padrão não inclui roles (evita lazy loading)
                .build();
    }

    /**
     * Converte Permission entity para PermissionResponse DTO incluindo roles
     * Use apenas quando roles já estiverem carregadas (EAGER ou JOIN FETCH)
     */
    public static PermissionResponse fromEntityWithRoles(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .active(permission.getActive())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .roles(
                    permission.getRoles() != null
                        ? permission.getRoles().stream()
                            .map(com.neuroefficiency.domain.model.Role::getName)
                            .collect(Collectors.toSet())
                        : Set.of()
                )
                .build();
    }
}

