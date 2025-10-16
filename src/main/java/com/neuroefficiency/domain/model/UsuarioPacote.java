package com.neuroefficiency.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade UsuarioPacote - Representa o pacote/plano de um usuário
 * 
 * Armazena metadados do pacote como limites, data de vencimento e permissões customizadas.
 * Permite flexibilidade total para diferentes tipos de pacotes e customizações por cliente.
 * 
 * @author Joao Fuhrmann
 * @version 1.0 - Fase 3 RBAC
 * @since 2025-10-16
 */
@Entity
@Table(name = "usuario_pacotes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPacote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Usuário é obrigatório")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @NotBlank(message = "Tipo do pacote é obrigatório")
    @Size(max = 50, message = "Tipo do pacote deve ter no máximo 50 caracteres")
    @Column(name = "pacote_type", nullable = false, length = 50)
    private String pacoteType; // BASIC, PREMIUM, ENTERPRISE, CUSTOM

    @PositiveOrZero(message = "Limite de pacientes deve ser positivo ou zero")
    @Column(name = "limite_pacientes")
    private Integer limitePacientes; // null = ilimitado

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento; // null = sem vencimento

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "permissoes_customizadas", columnDefinition = "TEXT")
    private String permissoesCustomizadas; // JSON string para CLINICO_CUSTOM

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
     * Verifica se o pacote está ativo e não vencido
     */
    public boolean isValido() {
        if (!this.ativo) {
            return false;
        }
        
        if (this.dataVencimento != null && this.dataVencimento.isBefore(LocalDate.now())) {
            return false;
        }
        
        return true;
    }

    /**
     * Verifica se o pacote tem limite de pacientes
     */
    public boolean hasLimitePacientes() {
        return this.limitePacientes != null && this.limitePacientes > 0;
    }

    /**
     * Verifica se o pacote é ilimitado
     */
    public boolean isIlimitado() {
        return this.limitePacientes == null || this.limitePacientes <= 0;
    }

    /**
     * Verifica se o pacote é customizado
     */
    public boolean isCustomizado() {
        return "CUSTOM".equals(this.pacoteType);
    }

    /**
     * Verifica se o pacote tem vencimento
     */
    public boolean hasVencimento() {
        return this.dataVencimento != null;
    }

    /**
     * Retorna dias até o vencimento (negativo se vencido)
     */
    public long getDiasAteVencimento() {
        if (this.dataVencimento == null) {
            return Long.MAX_VALUE; // Sem vencimento
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), this.dataVencimento);
    }

    /**
     * ToString customizado para não expor dados sensíveis em logs
     */
    @Override
    public String toString() {
        return "UsuarioPacote{" +
                "id=" + id +
                ", pacoteType='" + pacoteType + '\'' +
                ", limitePacientes=" + limitePacientes +
                ", dataVencimento=" + dataVencimento +
                ", ativo=" + ativo +
                ", isValido=" + isValido() +
                ", createdAt=" + createdAt +
                '}';
    }
}
