package com.neuroefficiency.dto.response;

import com.neuroefficiency.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO de resposta para Role
 * Evita problemas de lazy loading e serialização circular
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> permissions; // Apenas os nomes das permissions

    /**
     * Converte Role entity para RoleResponse DTO
     */
    public static RoleResponse fromEntity(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .permissions(null) // Por padrão não inclui permissions (evita lazy loading)
                .build();
    }

    /**
     * Converte Role entity para RoleResponse DTO incluindo permissions
     * Use apenas quando permissions já estiverem carregadas (EAGER ou JOIN FETCH)
     */
    public static RoleResponse fromEntityWithPermissions(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .permissions(
                    role.getPermissions() != null 
                        ? role.getPermissions().stream()
                            .map(com.neuroefficiency.domain.model.Permission::getName)
                            .collect(Collectors.toSet())
                        : Set.of()
                )
                .build();
    }
}

