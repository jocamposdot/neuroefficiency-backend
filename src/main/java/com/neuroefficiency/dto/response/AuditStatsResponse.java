package com.neuroefficiency.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neuroefficiency.domain.enums.AuditEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO de resposta para estatísticas de auditoria
 * 
 * Fornece métricas agregadas sobre logs de auditoria em um período.
 * 
 * @author Neuroefficiency Team
 * @version 4.0 - Fase 4: Audit Logging Avançado
 * @since 2025-11-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditStatsResponse {

    /**
     * Período analisado
     */
    @Builder.Default
    private DateRange period = new DateRange();

    /**
     * Total de eventos no período
     */
    @Builder.Default
    private Long totalEvents = 0L;

    /**
     * Total de eventos bem-sucedidos
     */
    @Builder.Default
    private Long successfulEvents = 0L;

    /**
     * Total de eventos com falha
     */
    @Builder.Default
    private Long failedEvents = 0L;

    /**
     * Taxa de sucesso (%)
     */
    @Builder.Default
    private Double successRate = 0.0;

    /**
     * Distribuição de eventos por tipo
     * Mapa: AuditEventType -> Quantidade
     */
    @Builder.Default
    private Map<AuditEventType, Long> eventsByType = Map.of();

    /**
     * Top usuários mais ativos
     */
    @Builder.Default
    private List<UserActivityStats> topUsers = List.of();

    /**
     * Total de usuários únicos que realizaram ações
     */
    @Builder.Default
    private Long uniqueUsers = 0L;

    /**
     * Total de tentativas de acesso negado
     */
    @Builder.Default
    private Long accessDeniedAttempts = 0L;

    /**
     * Total de eventos de segurança
     */
    @Builder.Default
    private Long securityEvents = 0L;

    /**
     * Total de eventos RBAC
     */
    @Builder.Default
    private Long rbacEvents = 0L;

    /**
     * Total de eventos de autenticação
     */
    @Builder.Default
    private Long authEvents = 0L;

    /**
     * Classe interna para representar período de análise
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRange {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }
}

