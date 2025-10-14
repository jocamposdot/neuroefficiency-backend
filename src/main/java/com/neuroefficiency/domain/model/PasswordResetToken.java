package com.neuroefficiency.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade PasswordResetToken - Armazena tokens de recuperação de senha
 * 
 * Tokens são hasheados com SHA-256 (não BCrypt) para permitir lookup direto.
 * Cada token tem expiração de 30 minutos e uso único.
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hash SHA-256 do token (64 caracteres hexadecimais)
     * 
     * IMPORTANTE: Usa SHA-256 ao invés de BCrypt porque:
     * - BCrypt usa salt aleatório, impossibilitando busca direta
     * - SHA-256 é determinístico (mesmo input = mesmo hash)
     * - Permite findByTokenHash() funcionar corretamente
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /**
     * Usuário que solicitou o reset de senha
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Data/hora de expiração do token (30 minutos após criação)
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Data/hora em que o token foi usado
     * NULL = token ainda não foi usado
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Data/hora de criação do token
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Método executado antes de persistir a entidade
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Verifica se o token está expirado
     * 
     * @return true se o token expirou
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Verifica se o token já foi usado
     * 
     * @return true se usedAt não é null
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * Verifica se o token é válido (não expirado e não usado)
     * 
     * @return true se o token pode ser usado
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /**
     * Marca o token como usado
     */
    public void markAsUsed() {
        this.usedAt = LocalDateTime.now();
    }

    /**
     * ToString customizado para não expor dados sensíveis
     */
    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + id +
                ", usuarioId=" + (usuario != null ? usuario.getId() : null) +
                ", expiresAt=" + expiresAt +
                ", used=" + isUsed() +
                ", expired=" + isExpired() +
                '}';
    }
}

