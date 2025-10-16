package com.neuroefficiency.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entidade Usuario - Representa um usuário do sistema Neuroefficiency
 * 
 * Implementa UserDetails do Spring Security para integração com o sistema de autenticação.
 * 
 * @author Joao Fuhrmann
 * @version 3.0 - Adicionado RBAC com roles e permissões (Fase 3)
 * @since 2025-10-11
 */
@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9_-]+$",
        message = "Username deve conter apenas letras, números, _ ou -"
    )
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // TAREFA 2: Campo email para recuperação de senha
    // Nullable para não quebrar usuários legacy da Fase 1
    @Email(message = "Email deve ser válido")
    @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
    @Column(unique = true, length = 255)  // Unique mas nullable
    private String email;

    @NotBlank(message = "Password é obrigatório")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;

    @Builder.Default
    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = true;

    @Builder.Default
    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===========================================
    // Relacionamentos RBAC (Fase 3)
    // ===========================================

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private UsuarioPacote pacote = null;

    /**
     * Método executado antes de persistir a entidade
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Método executado antes de atualizar a entidade
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===========================================
    // Implementação de UserDetails do Spring Security
    // ===========================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Fase 3: Implementação RBAC completa
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Adicionar roles
        authorities.addAll(this.roles);
        
        // Adicionar permissões das roles
        for (Role role : this.roles) {
            authorities.addAll(role.getPermissions());
        }
        
        // Adicionar permissões customizadas do pacote (se existir)
        if (this.pacote != null && this.pacote.getPermissoesCustomizadas() != null) {
            // TODO: Implementar parsing de JSON para permissões customizadas
            // Por enquanto, retorna as authorities das roles
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    // ===========================================
    // Métodos utilitários RBAC
    // ===========================================

    /**
     * Adiciona uma role ao usuário
     */
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsuarios().add(this);
    }

    /**
     * Remove uma role do usuário
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsuarios().remove(this);
    }

    /**
     * Verifica se o usuário tem uma role específica
     */
    public boolean hasRole(String roleName) {
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * Verifica se o usuário tem uma permissão específica
     */
    public boolean hasPermission(String permissionName) {
        return this.roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    /**
     * Verifica se o usuário é ADMIN
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Verifica se o usuário é CLINICO
     */
    public boolean isClinico() {
        return hasRole("CLINICO");
    }

    /**
     * Retorna o tipo do pacote do usuário
     */
    public String getPacoteType() {
        return this.pacote != null ? this.pacote.getPacoteType() : null;
    }

    /**
     * Verifica se o pacote do usuário está válido (ativo e não vencido)
     */
    public boolean isPacoteValido() {
        return this.pacote != null && this.pacote.isValido();
    }

    /**
     * Retorna o limite de pacientes do usuário
     */
    public Integer getLimitePacientes() {
        return this.pacote != null ? this.pacote.getLimitePacientes() : null;
    }

    /**
     * Verifica se o usuário tem limite de pacientes
     */
    public boolean hasLimitePacientes() {
        return this.pacote != null && this.pacote.hasLimitePacientes();
    }

    /**
     * ToString customizado para não expor dados sensíveis em logs
     */
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", enabled=" + enabled +
                ", roles=" + roles.stream().map(Role::getName).collect(Collectors.toSet()) +
                ", pacoteType=" + getPacoteType() +
                ", createdAt=" + createdAt +
                '}';
    }
}

