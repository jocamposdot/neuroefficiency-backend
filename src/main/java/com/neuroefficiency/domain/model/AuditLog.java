package com.neuroefficiency.domain.model;

import com.neuroefficiency.domain.enums.AuditEventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade AuditLog - Representa um registro de auditoria no sistema Neuroefficiency
 * 
 * Armazena todas as ações críticas realizadas no sistema para fins de:
 * - Rastreabilidade e compliance (LGPD)
 * - Segurança e detecção de anomalias
 * - Debugging e investigação de problemas
 * - Relatórios gerenciais
 * 
 * @author Neuroefficiency Team
 * @version 4.0 - Fase 4: Audit Logging Avançado
 * @since 2025-11-12
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_event_type", columnList = "event_type"),
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_user_timestamp", columnList = "user_id,timestamp"),
    @Index(name = "idx_audit_success", columnList = "success"),
    @Index(name = "idx_audit_target", columnList = "target_type,target_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    /**
     * Identificador único do log de auditoria
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo de evento auditado
     * Exemplos: AUTH_LOGIN, RBAC_ROLE_CREATED, SECURITY_ACCESS_DENIED
     */
    @NotNull(message = "Tipo de evento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AuditEventType eventType;

    /**
     * ID do usuário que executou a ação (null para eventos de sistema)
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Username do usuário que executou a ação (desnormalizado para performance)
     */
    @Size(max = 50, message = "Username deve ter no máximo 50 caracteres")
    @Column(name = "username", length = 50)
    private String username;

    /**
     * ID do recurso alvo da ação (ex: ID da role criada, ID do usuário modificado)
     */
    @Size(max = 100, message = "Target ID deve ter no máximo 100 caracteres")
    @Column(name = "target_id", length = 100)
    private String targetId;

    /**
     * Tipo do recurso alvo (ex: "Role", "Permission", "Usuario", "Pacote")
     */
    @Size(max = 50, message = "Target type deve ter no máximo 50 caracteres")
    @Column(name = "target_type", length = 50)
    private String targetType;

    /**
     * Descrição da ação realizada
     * Exemplo: "Criou role ADMIN", "Atribuiu permissão READ_USERS à role CLINICO"
     */
    @NotNull(message = "Ação é obrigatória")
    @Size(min = 1, max = 100, message = "Ação deve ter entre 1 e 100 caracteres")
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /**
     * Descrição detalhada do evento (legível para humanos)
     */
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Detalhes adicionais em formato JSON
     * Permite armazenar metadados customizados por tipo de evento
     * Exemplo: {"roleId": 5, "roleName": "MANAGER", "permissions": ["READ_USERS"]}
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * Endereço IP de origem da ação
     */
    @Size(max = 45, message = "IP address deve ter no máximo 45 caracteres") // IPv6 max length
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User-Agent do cliente (navegador, aplicação, etc.)
     */
    @Size(max = 255, message = "User agent deve ter no máximo 255 caracteres")
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /**
     * Indica se a ação foi bem-sucedida
     * true = sucesso, false = falha/erro
     */
    @NotNull(message = "Success é obrigatório")
    @Builder.Default
    @Column(name = "success", nullable = false)
    private Boolean success = true;

    /**
     * Mensagem de erro (se success = false)
     */
    @Size(max = 500, message = "Error message deve ter no máximo 500 caracteres")
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * Timestamp do evento (quando ocorreu)
     */
    @NotNull(message = "Timestamp é obrigatório")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Método executado antes de persistir a entidade
     * Garante que timestamp seja sempre preenchido
     */
    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    // ===========================================
    // MÉTODOS UTILITÁRIOS
    // ===========================================

    /**
     * Verifica se o evento é relacionado a autenticação
     * 
     * @return true se for evento de autenticação
     */
    public boolean isAuthEvent() {
        return this.eventType != null && this.eventType.isAuthEvent();
    }

    /**
     * Verifica se o evento é relacionado a RBAC
     * 
     * @return true se for evento de RBAC
     */
    public boolean isRbacEvent() {
        return this.eventType != null && this.eventType.isRbacEvent();
    }

    /**
     * Verifica se o evento é relacionado a segurança
     * 
     * @return true se for evento de segurança
     */
    public boolean isSecurityEvent() {
        return this.eventType != null && this.eventType.isSecurityEvent();
    }

    /**
     * Verifica se o evento é relacionado a sistema
     * 
     * @return true se for evento de sistema
     */
    public boolean isSystemEvent() {
        return this.eventType != null && this.eventType.isSystemEvent();
    }

    /**
     * Verifica se o log representa uma falha/erro
     * 
     * @return true se success = false
     */
    public boolean isFailure() {
        return !Boolean.TRUE.equals(this.success);
    }

    /**
     * Retorna a categoria do evento
     * 
     * @return Categoria do evento (ex: "Autenticação", "RBAC - Roles")
     */
    public String getEventCategory() {
        return this.eventType != null ? this.eventType.getCategory() : null;
    }

    /**
     * ToString customizado para logs (não expõe dados sensíveis em detalhes)
     */
    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", eventType=" + eventType +
                ", username='" + username + '\'' +
                ", action='" + action + '\'' +
                ", targetType='" + targetType + '\'' +
                ", targetId='" + targetId + '\'' +
                ", success=" + success +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Equals e HashCode baseados apenas no ID
     * Logs de auditoria são únicos por ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLog)) return false;
        AuditLog auditLog = (AuditLog) o;
        return id != null && id.equals(auditLog.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

