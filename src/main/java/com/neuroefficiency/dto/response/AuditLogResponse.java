package com.neuroefficiency.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neuroefficiency.domain.enums.AuditEventType;
import com.neuroefficiency.domain.model.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de resposta para logs de auditoria
 * 
 * Representa um log de auditoria de forma otimizada para resposta de API,
 * sem expor detalhes internos da entidade.
 * 
 * @author Neuroefficiency Team
 * @version 4.0 - Fase 4: Audit Logging Avançado
 * @since 2025-11-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    /**
     * ID único do log
     */
    private Long id;

    /**
     * Tipo de evento
     */
    private AuditEventType eventType;

    /**
     * Categoria do evento (ex: "Autenticação", "RBAC - Roles")
     */
    private String eventCategory;

    /**
     * Descrição do tipo de evento
     */
    private String eventDescription;

    /**
     * ID do usuário que executou a ação
     */
    private Long userId;

    /**
     * Username do usuário que executou a ação
     */
    private String username;

    /**
     * ID do recurso alvo
     */
    private String targetId;

    /**
     * Tipo do recurso alvo
     */
    private String targetType;

    /**
     * Descrição da ação
     */
    private String action;

    /**
     * Descrição detalhada do evento
     */
    private String description;

    /**
     * Detalhes adicionais em JSON
     */
    private String details;

    /**
     * IP de origem
     */
    private String ipAddress;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * Indica se a operação foi bem-sucedida
     */
    private Boolean success;

    /**
     * Mensagem de erro (se houver)
     */
    private String errorMessage;

    /**
     * Timestamp do evento
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Converte uma entidade AuditLog para DTO de resposta
     * 
     * @param auditLog Entidade AuditLog
     * @return DTO de resposta
     */
    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .eventType(auditLog.getEventType())
                .eventCategory(auditLog.getEventCategory())
                .eventDescription(auditLog.getEventType() != null ? 
                                 auditLog.getEventType().getDescription() : null)
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .targetId(auditLog.getTargetId())
                .targetType(auditLog.getTargetType())
                .action(auditLog.getAction())
                .description(auditLog.getDescription())
                .details(auditLog.getDetails())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .success(auditLog.getSuccess())
                .errorMessage(auditLog.getErrorMessage())
                .timestamp(auditLog.getTimestamp())
                .build();
    }

    /**
     * Converte uma entidade AuditLog para DTO de resposta (versão resumida)
     * Omite detalhes como userAgent e details para listagens
     * 
     * @param auditLog Entidade AuditLog
     * @return DTO de resposta resumido
     */
    public static AuditLogResponse fromEntitySummary(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .eventType(auditLog.getEventType())
                .eventCategory(auditLog.getEventCategory())
                .eventDescription(auditLog.getEventType() != null ? 
                                 auditLog.getEventType().getDescription() : null)
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .targetId(auditLog.getTargetId())
                .targetType(auditLog.getTargetType())
                .action(auditLog.getAction())
                .description(auditLog.getDescription())
                .ipAddress(auditLog.getIpAddress())
                .success(auditLog.getSuccess())
                .errorMessage(auditLog.getErrorMessage())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}

