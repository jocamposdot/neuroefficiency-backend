package com.neuroefficiency.domain.repository;

import com.neuroefficiency.domain.enums.AuditEventType;
import com.neuroefficiency.domain.model.PasswordResetAudit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para a entidade PasswordResetAudit
 * 
 * Fornece operações de acesso a dados para auditoria de reset de senha.
 * Essencial para:
 * - Rate limiting (3 tentativas/hora)
 * - Compliance LGPD
 * - Análise de segurança
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@Repository
public interface PasswordResetAuditRepository extends JpaRepository<PasswordResetAudit, Long> {

    /**
     * Conta tentativas de um email em um período
     * 
     * Usado para rate limiting: máximo 3 tentativas por hora por email.
     * 
     * @param email email a ser verificado
     * @param after data/hora de início do período
     * @return número de tentativas após 'after'
     */
    long countByEmailAndTimestampAfter(String email, LocalDateTime after);

    /**
     * Conta tentativas de um IP em um período
     * 
     * Usado para rate limiting: máximo 3 tentativas por hora por IP.
     * 
     * @param ipAddress endereço IP a ser verificado
     * @param after data/hora de início do período
     * @return número de tentativas após 'after'
     */
    long countByIpAddressAndTimestampAfter(String ipAddress, LocalDateTime after);

    /**
     * Busca histórico de tentativas de um email (mais recentes primeiro)
     * 
     * Usado para análise de segurança e investigação de abusos.
     * 
     * @param email email a ser pesquisado
     * @param pageable configuração de paginação
     * @return lista paginada de auditorias
     */
    List<PasswordResetAudit> findByEmailOrderByTimestampDesc(String email, Pageable pageable);

    /**
     * Busca histórico de tentativas de um IP (mais recentes primeiro)
     * 
     * Usado para detectar padrões de ataque.
     * 
     * @param ipAddress endereço IP a ser pesquisado
     * @param pageable configuração de paginação
     * @return lista paginada de auditorias
     */
    List<PasswordResetAudit> findByIpAddressOrderByTimestampDesc(String ipAddress, Pageable pageable);

    /**
     * Busca tentativas por tipo de evento em um período
     * 
     * Usado para métricas e dashboards.
     * 
     * @param eventType tipo de evento
     * @param after data/hora de início do período
     * @return lista de auditorias do tipo especificado
     */
    List<PasswordResetAudit> findByEventTypeAndTimestampAfter(
        AuditEventType eventType,
        LocalDateTime after
    );

    /**
     * Conta tentativas bem-sucedidas em um período
     * 
     * Usado para métricas de uso do sistema.
     * 
     * @param after data/hora de início do período
     * @return número de resets bem-sucedidos
     */
    @Query("SELECT COUNT(a) FROM PasswordResetAudit a " +
           "WHERE a.success = true " +
           "AND a.timestamp > :after")
    long countSuccessfulResetsAfter(@Param("after") LocalDateTime after);

    /**
     * Conta tentativas falhadas em um período
     * 
     * Usado para detectar ataques e problemas.
     * 
     * @param after data/hora de início do período
     * @return número de tentativas falhadas
     */
    @Query("SELECT COUNT(a) FROM PasswordResetAudit a " +
           "WHERE a.success = false " +
           "AND a.timestamp > :after")
    long countFailedAttemptsAfter(@Param("after") LocalDateTime after);

    /**
     * Busca IPs com mais tentativas em um período (suspeitos)
     * 
     * Usado para detecção automática de ataques.
     * 
     * @param after data/hora de início do período
     * @param pageable configuração de paginação (top N)
     * @return lista de IPs ordenados por número de tentativas (desc)
     */
    @Query("SELECT a.ipAddress, COUNT(a) as attempts " +
           "FROM PasswordResetAudit a " +
           "WHERE a.timestamp > :after " +
           "GROUP BY a.ipAddress " +
           "ORDER BY attempts DESC")
    List<Object[]> findTopAttackerIPs(@Param("after") LocalDateTime after, Pageable pageable);

    /**
     * Deleta registros antigos de auditoria (cleanup periódico)
     * 
     * Compliance LGPD: manter apenas dados necessários.
     * Executado mensalmente via job.
     * 
     * @param before data/hora limite (manter apenas depois desta data)
     * @return número de registros deletados
     */
    @Query("DELETE FROM PasswordResetAudit a WHERE a.timestamp < :before")
    int deleteOldAudits(@Param("before") LocalDateTime before);
}

