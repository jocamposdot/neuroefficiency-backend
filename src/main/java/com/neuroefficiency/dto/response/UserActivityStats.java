package com.neuroefficiency.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estatísticas de atividade de um usuário
 * 
 * Usado em relatórios de auditoria para mostrar usuários mais ativos.
 * 
 * @author Neuroefficiency Team
 * @version 4.0 - Fase 4: Audit Logging Avançado
 * @since 2025-11-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityStats {

    /**
     * ID do usuário
     */
    private Long userId;

    /**
     * Username do usuário
     */
    private String username;

    /**
     * Quantidade total de ações realizadas
     */
    @Builder.Default
    private Long totalActions = 0L;

    /**
     * Quantidade de ações bem-sucedidas
     */
    @Builder.Default
    private Long successfulActions = 0L;

    /**
     * Quantidade de ações com falha
     */
    @Builder.Default
    private Long failedActions = 0L;

    /**
     * Taxa de sucesso (%)
     */
    @Builder.Default
    private Double successRate = 0.0;

    /**
     * Último acesso do usuário
     */
    private String lastActivity;

    /**
     * Calcula a taxa de sucesso automaticamente
     */
    public void calculateSuccessRate() {
        if (totalActions > 0) {
            this.successRate = (successfulActions * 100.0) / totalActions;
        } else {
            this.successRate = 0.0;
        }
    }
}

