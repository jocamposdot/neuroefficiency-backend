package com.neuroefficiency.domain.repository;

import com.neuroefficiency.domain.enums.AuditEventType;
import com.neuroefficiency.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para AuditLog - Operações de persistência e consulta de logs de auditoria
 * 
 * Fornece queries customizadas para:
 * - Consultas por usuário, tipo de evento, período
 * - Estatísticas e agregações
 * - Filtros complexos com paginação
 * 
 * @author Neuroefficiency Team
 * @version 4.0 - Fase 4: Audit Logging Avançado
 * @since 2025-11-12
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // ===========================================
    // CONSULTAS POR USUÁRIO
    // ===========================================

    /**
     * Busca logs de um usuário específico (paginado)
     * 
     * @param userId ID do usuário
     * @param pageable Configuração de paginação e ordenação
     * @return Página de logs do usuário
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Busca logs de um usuário em um período específico
     * 
     * @param userId ID do usuário
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Configuração de paginação
     * @return Página de logs do usuário no período
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId " +
           "AND a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Conta quantos logs um usuário tem
     * 
     * @param userId ID do usuário
     * @return Quantidade de logs
     */
    Long countByUserId(Long userId);

    // ===========================================
    // CONSULTAS POR TIPO DE EVENTO
    // ===========================================

    /**
     * Busca logs por tipo de evento (paginado)
     * 
     * @param eventType Tipo do evento
     * @param pageable Configuração de paginação
     * @return Página de logs do tipo especificado
     */
    Page<AuditLog> findByEventType(AuditEventType eventType, Pageable pageable);

    /**
     * Busca logs por tipo de evento em um período
     * 
     * @param eventType Tipo do evento
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Configuração de paginação
     * @return Página de logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.eventType = :eventType " +
           "AND a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByEventTypeAndDateRange(
        @Param("eventType") AuditEventType eventType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Conta logs por tipo de evento
     * 
     * @param eventType Tipo do evento
     * @return Quantidade de logs
     */
    Long countByEventType(AuditEventType eventType);

    // ===========================================
    // CONSULTAS POR PERÍODO
    // ===========================================

    /**
     * Busca todos os logs em um período (paginado)
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Configuração de paginação
     * @return Página de logs no período
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Busca todos os logs em um período (sem paginação) - usar com cuidado!
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Lista de logs (pode ser grande!)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findAllByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // ===========================================
    // CONSULTAS DE SEGURANÇA
    // ===========================================

    /**
     * Busca logs de tentativas de acesso negado (paginado)
     * 
     * @param pageable Configuração de paginação
     * @return Página de logs de acesso negado
     */
    @Query("SELECT a FROM AuditLog a WHERE a.eventType = 'SECURITY_ACCESS_DENIED' " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findAccessDeniedLogs(Pageable pageable);

    /**
     * Busca logs de acesso negado em um período
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Configuração de paginação
     * @return Página de logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.eventType = 'SECURITY_ACCESS_DENIED' " +
           "AND a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findAccessDeniedLogsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Busca todos os logs de segurança (eventos que começam com SECURITY_)
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Configuração de paginação
     * @return Página de logs de segurança
     */
    @Query("SELECT a FROM AuditLog a WHERE a.eventType IN " +
           "('SECURITY_ACCESS_DENIED', 'SECURITY_UNAUTHORIZED_ATTEMPT', " +
           "'SECURITY_SUSPICIOUS_ACTIVITY', 'SECURITY_INVALID_TOKEN', " +
           "'SECURITY_RATE_LIMIT_EXCEEDED') " +
           "AND a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findSecurityLogsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Busca logs de falhas (success = false)
     * 
     * @param pageable Configuração de paginação
     * @return Página de logs de falhas
     */
    Page<AuditLog> findBySuccessFalse(Pageable pageable);

    /**
     * Busca logs de falhas em um período
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Configuração de paginação
     * @return Página de logs de falhas
     */
    @Query("SELECT a FROM AuditLog a WHERE a.success = false " +
           "AND a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findFailuresByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    // ===========================================
    // CONSULTAS POR RECURSO ALVO
    // ===========================================

    /**
     * Busca logs relacionados a um recurso específico
     * 
     * @param targetType Tipo do recurso (ex: "Role", "Permission")
     * @param targetId ID do recurso
     * @param pageable Configuração de paginação
     * @return Página de logs do recurso
     */
    @Query("SELECT a FROM AuditLog a WHERE a.targetType = :targetType " +
           "AND a.targetId = :targetId ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTarget(
        @Param("targetType") String targetType,
        @Param("targetId") String targetId,
        Pageable pageable
    );

    // ===========================================
    // ESTATÍSTICAS E AGREGAÇÕES
    // ===========================================

    /**
     * Conta total de logs em um período
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Quantidade de logs
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    Long countByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Conta logs de sucesso em um período
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Quantidade de logs bem-sucedidos
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.success = true " +
           "AND a.timestamp BETWEEN :startDate AND :endDate")
    Long countSuccessByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Conta logs de falha em um período
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Quantidade de logs com falha
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.success = false " +
           "AND a.timestamp BETWEEN :startDate AND :endDate")
    Long countFailuresByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Retorna os top N usuários mais ativos em um período
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @param limit Quantidade de usuários a retornar
     * @return Lista de [userId, username, count]
     */
    @Query("SELECT a.userId, a.username, COUNT(a) FROM AuditLog a " +
           "WHERE a.userId IS NOT NULL " +
           "AND a.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY a.userId, a.username " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findTopActiveUsers(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Retorna distribuição de eventos por tipo em um período
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Lista de [eventType, count]
     */
    @Query("SELECT a.eventType, COUNT(a) FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY a.eventType " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> countByEventTypeInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Retorna eventos mais recentes (para dashboard)
     * 
     * @param limit Quantidade de eventos a retornar
     * @return Lista dos últimos N eventos
     */
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentEvents(Pageable pageable);

    // ===========================================
    // LIMPEZA E MANUTENÇÃO
    // ===========================================

    /**
     * Deleta logs mais antigos que uma data específica
     * Usado para arquivamento/limpeza de dados antigos
     * 
     * @param date Data limite (logs anteriores serão deletados)
     * @return Quantidade de logs deletados
     */
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :date")
    Long deleteByTimestampBefore(@Param("date") LocalDateTime date);

    /**
     * Conta logs mais antigos que uma data (para verificar antes de deletar)
     * 
     * @param date Data limite
     * @return Quantidade de logs que seriam deletados
     */
    Long countByTimestampBefore(LocalDateTime date);
}

