package com.neuroefficiency.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade Role - Representa um papel/função no sistema
 * 
 * Implementa GrantedAuthority do Spring Security para integração com autorização.
 * Roles são a base do sistema RBAC (Role-Based Access Control).
 * 
 * @author Joao Fuhrmann
 * @version 1.0 - Fase 3 RBAC
 * @since 2025-10-16
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da role é obrigatório")
    @Size(min = 2, max = 50, message = "Nome da role deve ter entre 2 e 50 caracteres")
    @Pattern(
        regexp = "^[A-Z_]+$",
        message = "Nome da role deve conter apenas letras maiúsculas e underscore"
    )
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===========================================
    // Relacionamentos
    // ===========================================

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Usuario> usuarios = new HashSet<>();

    // ===========================================
    // Implementação de GrantedAuthority
    // ===========================================

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name;
    }

    // ===========================================
    // Métodos de ciclo de vida
    // ===========================================

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===========================================
    // Métodos utilitários
    // ===========================================

    /**
     * Adiciona uma permissão à role
     */
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }

    /**
     * Remove uma permissão da role
     */
    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }

    /**
     * Verifica se a role tem uma permissão específica
     */
    public boolean hasPermission(String permissionName) {
        return this.permissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    /**
     * ToString customizado para não expor dados sensíveis em logs
     */
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
