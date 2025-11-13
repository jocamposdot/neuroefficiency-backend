package com.neuroefficiency.service;

import com.neuroefficiency.domain.enums.AuditEventType;
import com.neuroefficiency.domain.model.AuditLog;
import com.neuroefficiency.domain.model.Usuario;
import com.neuroefficiency.domain.repository.AuditLogRepository;
import com.neuroefficiency.dto.response.AuditLogResponse;
import com.neuroefficiency.dto.response.AuditStatsResponse;
import com.neuroefficiency.dto.response.UserActivityStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service para gerenciamento de auditoria no sistema Neuroefficiency
 * 
 * Responsável por:
 * - Registrar eventos de auditoria
 * - Consultar logs com diversos filtros
 * - Calcular estatísticas e métricas
 * - Exportar dados para compliance
 * 
 * @author Neuroefficiency Team
 * @version 4.0 - Fase 4: Audit Logging Avançado
 * @since 2025-11-12
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // ===========================================
    // REGISTRO DE EVENTOS
    // ===========================================

    /**
     * Registra um evento de auditoria
     * 
     * @param eventType Tipo do evento
     * @param action Descrição da ação
     * @param description Descrição detalhada
     * @param targetType Tipo do recurso alvo
     * @param targetId ID do recurso alvo
     * @param details Detalhes em JSON
     * @param ipAddress IP de origem
     * @param userAgent User-Agent
     * @param success Se a operação foi bem-sucedida
     * @param errorMessage Mensagem de erro (se houver)
     * @return Log criado
     */
    public AuditLog log(
            AuditEventType eventType,
            String action,
            String description,
            String targetType,
            String targetId,
            String details,
            String ipAddress,
            String userAgent,
            Boolean success,
            String errorMessage
    ) {
        // Obter usuário do contexto de segurança
        Usuario currentUser = getCurrentUser();
        
        AuditLog auditLog = AuditLog.builder()
                .eventType(eventType)
                .userId(currentUser != null ? currentUser.getId() : null)
                .username(currentUser != null ? currentUser.getUsername() : "SYSTEM")
                .targetId(targetId)
                .targetType(targetType)
                .action(action)
                .description(description)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success != null ? success : true)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();

        AuditLog savedLog = auditLogRepository.save(auditLog);
        
        log.debug("Auditoria registrada: {} - {} (ID: {})", 
                 eventType, action, savedLog.getId());
        
        return savedLog;
    }

    /**
     * Registra um evento de auditoria (versão simplificada para sucesso)
     * 
     * @param eventType Tipo do evento
     * @param action Descrição da ação
     * @param targetType Tipo do recurso alvo
     * @param targetId ID do recurso alvo
     * @return Log criado
     */
    public AuditLog logSuccess(
            AuditEventType eventType,
            String action,
            String targetType,
            String targetId
    ) {
        return log(eventType, action, null, targetType, targetId, 
                  null, null, null, true, null);
    }

    /**
     * Registra um evento de auditoria de falha
     * 
     * @param eventType Tipo do evento
     * @param action Descrição da ação
     * @param errorMessage Mensagem de erro
     * @return Log criado
     */
    public AuditLog logFailure(
            AuditEventType eventType,
            String action,
            String errorMessage
    ) {
        return log(eventType, action, null, null, null, 
                  null, null, null, false, errorMessage);
    }

    /**
     * Registra um evento de acesso negado
     * 
     * @param resource Recurso que foi tentado acessar
     * @param reason Razão da negação
     * @param ipAddress IP de origem
     * @return Log criado
     */
    public AuditLog logAccessDenied(String resource, String reason, String ipAddress) {
        return log(
            AuditEventType.SECURITY_ACCESS_DENIED,
            "Tentativa de acesso a: " + resource,
            reason,
            "Resource",
            resource,
            null,
            ipAddress,
            null,
            false,
            reason
        );
    }

    // ===========================================
    // CONSULTAS DE LOGS
    // ===========================================

    /**
     * Busca todos os logs (paginado)
     * 
     * @param pageable Configuração de paginação
     * @return Página de logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAllLogs(Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return logs.map(AuditLogResponse::fromEntitySummary);
    }

    /**
     * Busca log por ID
     * 
     * @param id ID do log
     * @return Log encontrado
     */
    @Transactional(readOnly = true)
    public AuditLogResponse findLogById(Long id) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log não encontrado: " + id));
        return AuditLogResponse.fromEntity(log);
    }

    /**
     * Busca logs de um usuário
     * 
     * @param userId ID do usuário
     * @param pageable Configuração de paginação
     * @return Página de logs do usuário
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findLogsByUser(Long userId, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByUserId(userId, pageable);
        return logs.map(AuditLogResponse::fromEntitySummary);
    }

    /**
     * Busca logs de um usuário em um período
     * 
     * @param userId ID do usuário
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Configuração de paginação
     * @return Página de logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findLogsByUserAndDateRange(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        Page<AuditLog> logs = auditLogRepository.findByUserIdAndDateRange(
            userId, start, end, pageable);
        return logs.map(AuditLogResponse::fromEntitySummary);
    }

    /**
     * Busca logs por tipo de evento
     * 
     * @param eventType Tipo do evento
     * @param pageable Configuração de paginação
     * @return Página de logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findLogsByEventType(
            AuditEventType eventType,
            Pageable pageable
    ) {
        Page<AuditLog> logs = auditLogRepository.findByEventType(eventType, pageable);
        return logs.map(AuditLogResponse::fromEntitySummary);
    }

    /**
     * Busca logs por tipo de evento em um período
     * 
     * @param eventType Tipo do evento
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Configuração de paginação
     * @return Página de logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findLogsByEventTypeAndDateRange(
            AuditEventType eventType,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        Page<AuditLog> logs = auditLogRepository.findByEventTypeAndDateRange(
            eventType, start, end, pageable);
        return logs.map(AuditLogResponse::fromEntitySummary);
    }

    /**
     * Busca logs em um período
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Configuração de paginação
     * @return Página de logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findLogsByDateRange(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        Page<AuditLog> logs = auditLogRepository.findByDateRange(start, end, pageable);
        return logs.map(AuditLogResponse::fromEntitySummary);
    }

    /**
     * Busca logs de tentativas de acesso negado
     * 
     * @param startDate Data inicial (opcional)
     * @param endDate Data final (opcional)
     * @param pageable Configuração de paginação
     * @return Página de logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAccessDeniedLogs(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        Page<AuditLog> logs;
        
        if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            logs = auditLogRepository.findAccessDeniedLogsByDateRange(start, end, pageable);
        } else {
            logs = auditLogRepository.findAccessDeniedLogs(pageable);
        }
        
        return logs.map(AuditLogResponse::fromEntity);
    }

    /**
     * Busca logs de segurança em um período
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Configuração de paginação
     * @return Página de logs de segurança
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findSecurityLogs(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        Page<AuditLog> logs = auditLogRepository.findSecurityLogsByDateRange(
            start, end, pageable);
        return logs.map(AuditLogResponse::fromEntitySummary);
    }

    // ===========================================
    // ESTATÍSTICAS
    // ===========================================

    /**
     * Calcula estatísticas de auditoria em um período
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Estatísticas calculadas
     */
    @Transactional(readOnly = true)
    public AuditStatsResponse getStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        // Contar total de eventos
        Long totalEvents = auditLogRepository.countByDateRange(start, end);
        
        // Contar sucessos e falhas
        Long successfulEvents = auditLogRepository.countSuccessByDateRange(start, end);
        Long failedEvents = auditLogRepository.countFailuresByDateRange(start, end);
        
        // Calcular taxa de sucesso
        Double successRate = totalEvents > 0 ? 
            (successfulEvents * 100.0) / totalEvents : 0.0;

        // Distribuição por tipo de evento
        List<Object[]> eventTypeCounts = auditLogRepository
            .countByEventTypeInDateRange(start, end);
        Map<AuditEventType, Long> eventsByType = new HashMap<>();
        for (Object[] row : eventTypeCounts) {
            eventsByType.put((AuditEventType) row[0], (Long) row[1]);
        }

        // Top usuários
        Pageable topUsersPageable = PageRequest.of(0, 10);
        List<Object[]> topUsersData = auditLogRepository
            .findTopActiveUsers(start, end, topUsersPageable);
        List<UserActivityStats> topUsers = topUsersData.stream()
            .map(row -> UserActivityStats.builder()
                    .userId((Long) row[0])
                    .username((String) row[1])
                    .totalActions((Long) row[2])
                    .build())
            .collect(Collectors.toList());

        // Contar eventos por categoria
        Long authEvents = eventsByType.entrySet().stream()
            .filter(e -> e.getKey().isAuthEvent())
            .mapToLong(Map.Entry::getValue)
            .sum();
            
        Long rbacEvents = eventsByType.entrySet().stream()
            .filter(e -> e.getKey().isRbacEvent())
            .mapToLong(Map.Entry::getValue)
            .sum();
            
        Long securityEvents = eventsByType.entrySet().stream()
            .filter(e -> e.getKey().isSecurityEvent())
            .mapToLong(Map.Entry::getValue)
            .sum();

        Long accessDeniedAttempts = eventsByType.getOrDefault(
            AuditEventType.SECURITY_ACCESS_DENIED, 0L);

        return AuditStatsResponse.builder()
                .period(AuditStatsResponse.DateRange.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .build())
                .totalEvents(totalEvents)
                .successfulEvents(successfulEvents)
                .failedEvents(failedEvents)
                .successRate(successRate)
                .eventsByType(eventsByType)
                .topUsers(topUsers)
                .uniqueUsers((long) topUsers.size())
                .accessDeniedAttempts(accessDeniedAttempts)
                .securityEvents(securityEvents)
                .rbacEvents(rbacEvents)
                .authEvents(authEvents)
                .build();
    }

    // ===========================================
    // EXPORTAÇÃO
    // ===========================================

    /**
     * Exporta logs para CSV
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @param eventType Tipo de evento (opcional)
     * @return String CSV
     */
    @Transactional(readOnly = true)
    public String exportToCsv(
            LocalDate startDate,
            LocalDate endDate,
            AuditEventType eventType
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<AuditLog> logs;
        if (eventType != null) {
            Pageable pageable = Pageable.unpaged();
            logs = auditLogRepository.findByEventTypeAndDateRange(
                eventType, start, end, pageable).getContent();
        } else {
            logs = auditLogRepository.findAllByDateRange(start, end);
        }

        StringBuilder csv = new StringBuilder();
        // Header
        csv.append("ID,Event Type,Event Category,Username,Action,Target Type,Target ID,")
           .append("Success,IP Address,Timestamp,Error Message\n");

        // Data
        for (AuditLog log : logs) {
            csv.append(log.getId()).append(",")
               .append(log.getEventType()).append(",")
               .append(escapeCsv(log.getEventCategory())).append(",")
               .append(escapeCsv(log.getUsername())).append(",")
               .append(escapeCsv(log.getAction())).append(",")
               .append(escapeCsv(log.getTargetType())).append(",")
               .append(escapeCsv(log.getTargetId())).append(",")
               .append(log.getSuccess()).append(",")
               .append(escapeCsv(log.getIpAddress())).append(",")
               .append(log.getTimestamp()).append(",")
               .append(escapeCsv(log.getErrorMessage())).append("\n");
        }

        return csv.toString();
    }

    /**
     * Escapa valores para CSV (adiciona aspas se necessário)
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ===========================================
    // MÉTODOS AUXILIARES
    // ===========================================

    /**
     * Obtém o usuário atual do contexto de segurança
     * 
     * @return Usuário atual ou null
     */
    private Usuario getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder
                .getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof Usuario) {
                return (Usuario) authentication.getPrincipal();
            }
        } catch (Exception e) {
            log.warn("Erro ao obter usuário do contexto de segurança", e);
        }
        return null;
    }
}

