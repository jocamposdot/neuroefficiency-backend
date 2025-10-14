package com.neuroefficiency.domain.model;

import com.neuroefficiency.domain.enums.AuditEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade PasswordResetAudit - Auditoria de tentativas de reset de senha
 * 
 * Registra TODAS as tentativas de recuperação de senha para:
 * - Compliance LGPD (rastreabilidade)
 * - Rate limiting (proteção contra abuso)
 * - Análise de segurança
 * - Detecção de ataques
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@Entity
@Table(name = "password_reset_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email usado na tentativa
     * Armazenado mesmo se o email não existir (para rate limiting)
     */
    @Column(nullable = false, length = 255)
    private String email;

    /**
     * Endereço IP de origem (IPv4 ou IPv6)
     * Usado para rate limiting por IP
     */
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /**
     * User-Agent do navegador/cliente
     * Útil para análise de padrões de ataque
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Tipo de evento auditado
     * Ver {@link AuditEventType} para detalhes
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AuditEventType eventType;

    /**
     * Indica se a operação foi bem-sucedida
     */
    @Column(nullable = false)
    private Boolean success;

    /**
     * Mensagem de erro (se houver)
     * NULL se success = true
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Data/hora do evento
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Método executado antes de persistir a entidade
     */
    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    /**
     * ToString customizado para logs
     */
    @Override
    public String toString() {
        return "PasswordResetAudit{" +
                "id=" + id +
                ", email='" + sanitizeEmail(email) + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", eventType=" + eventType +
                ", success=" + success +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Sanitiza email para logs (previne exposição completa)
     */
    private String sanitizeEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        if (parts[0].isEmpty()) {
            return "***@" + parts[1];
        }
        return parts[0].charAt(0) + "***@" + parts[1];
    }
}

