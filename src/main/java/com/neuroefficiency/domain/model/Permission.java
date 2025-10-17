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
 * Entidade Permission - Representa uma permissão específica no sistema
 * 
 * Implementa GrantedAuthority do Spring Security para integração com autorização.
 * Permissões são os controles granulares de acesso (ex: READ_PATIENTS, WRITE_REPORTS).
 * 
 * @author Joao Fuhrmann
 * @version 1.0 - Fase 3 RBAC
 * @since 2025-10-16
 */
@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da permissão é obrigatório")
    @Size(min = 2, max = 100, message = "Nome da permissão deve ter entre 2 e 100 caracteres")
    @Pattern(
        regexp = "^[A-Z_]+$",
        message = "Nome da permissão deve conter apenas letras maiúsculas e underscore"
    )
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    @Column(length = 255)
    private String description;

    @NotBlank(message = "Recurso é obrigatório")
    @Size(max = 50, message = "Recurso deve ter no máximo 50 caracteres")
    @Column(nullable = false, length = 50)
    private String resource; // Ex: "patients", "reports", "api"

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

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // ===========================================
    // Implementação de GrantedAuthority
    // ===========================================

    @Override
    public String getAuthority() {
        return this.name;
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
     * Verifica se a permissão é para um recurso específico
     */
    public boolean isForResource(String resourceName) {
        return this.resource.equals(resourceName);
    }

    /**
     * Verifica se a permissão é de leitura
     */
    public boolean isReadPermission() {
        return this.name.startsWith("READ_");
    }

    /**
     * Verifica se a permissão é de escrita
     */
    public boolean isWritePermission() {
        return this.name.startsWith("WRITE_") || this.name.startsWith("CREATE_") || this.name.startsWith("UPDATE_") || this.name.startsWith("DELETE_");
    }

    /**
     * ToString customizado para não expor dados sensíveis em logs
     */
    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * Equals e HashCode customizados para evitar referência circular
     * Baseado apenas no name (chave de negócio)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission permission = (Permission) o;
        return name != null && name.equals(permission.name);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
