package com.neuroefficiency.controller;

import com.neuroefficiency.domain.enums.AuditEventType;
import com.neuroefficiency.dto.response.ApiResponse;
import com.neuroefficiency.dto.response.AuditLogResponse;
import com.neuroefficiency.dto.response.AuditStatsResponse;
import com.neuroefficiency.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Controller para endpoints de Auditoria (Admin)
 * 
 * Fornece acesso a logs de auditoria e estatísticas do sistema.
 * Todos os endpoints são protegidos e requerem role ADMIN.
 * 
 * @author Neuroefficiency Team
 * @version 4.0 - Fase 4: Audit Logging Avançado
 * @since 2025-11-12
 */
@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    // ===========================================
    // CONSULTA DE LOGS
    // ===========================================

    /**
     * Lista todos os logs de auditoria (paginado)
     * 
     * GET /api/admin/audit/logs?page=0&size=20&sort=timestamp,desc
     * 
     * @param page Número da página (default: 0)
     * @param size Tamanho da página (default: 20)
     * @param sort Ordenação (default: timestamp,desc)
     * @return Página de logs
     */
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort
    ) {
        log.info("Listando logs de auditoria - page: {}, size: {}", page, size);

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<AuditLogResponse> logs = auditService.findAllLogs(pageable);

        return ResponseEntity.ok(ApiResponse.success(
            logs,
            "Logs de auditoria obtidos com sucesso"
        ));
    }

    /**
     * Obtém um log específico por ID
     * 
     * GET /api/admin/audit/logs/{id}
     * 
     * @param id ID do log
     * @return Log encontrado
     */
    @GetMapping("/logs/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getLogById(@PathVariable Long id) {
        log.info("Buscando log de auditoria por ID: {}", id);

        AuditLogResponse auditLog = auditService.findLogById(id);

        return ResponseEntity.ok(ApiResponse.success(
            auditLog,
            "Log de auditoria encontrado"
        ));
    }

    /**
     * Lista logs de um usuário específico
     * 
     * GET /api/admin/audit/logs/user/{userId}?page=0&size=20
     * 
     * @param userId ID do usuário
     * @param page Número da página
     * @param size Tamanho da página
     * @param startDate Data inicial (opcional)
     * @param endDate Data final (opcional)
     * @return Página de logs do usuário
     */
    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Listando logs do usuário: {} (período: {} a {})", userId, startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLogResponse> logs;

        if (startDate != null && endDate != null) {
            logs = auditService.findLogsByUserAndDateRange(userId, startDate, endDate, pageable);
        } else {
            logs = auditService.findLogsByUser(userId, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(
            logs,
            "Logs do usuário obtidos com sucesso"
        ));
    }

    /**
     * Lista logs por tipo de evento
     * 
     * GET /api/admin/audit/logs/type/{eventType}?page=0&size=20
     * 
     * @param eventType Tipo do evento (enum)
     * @param page Número da página
     * @param size Tamanho da página
     * @param startDate Data inicial (opcional)
     * @param endDate Data final (opcional)
     * @return Página de logs
     */
    @GetMapping("/logs/type/{eventType}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogsByEventType(
            @PathVariable AuditEventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Listando logs do tipo: {} (período: {} a {})", eventType, startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLogResponse> logs;

        if (startDate != null && endDate != null) {
            logs = auditService.findLogsByEventTypeAndDateRange(eventType, startDate, endDate, pageable);
        } else {
            logs = auditService.findLogsByEventType(eventType, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(
            logs,
            "Logs por tipo de evento obtidos com sucesso"
        ));
    }

    /**
     * Lista logs em um período específico
     * 
     * GET /api/admin/audit/logs/date-range?startDate=2025-11-01&endDate=2025-11-30
     * 
     * @param startDate Data inicial (obrigatório)
     * @param endDate Data final (obrigatório)
     * @param page Número da página
     * @param size Tamanho da página
     * @return Página de logs
     */
    @GetMapping("/logs/date-range")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Listando logs no período: {} a {}", startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLogResponse> logs = auditService.findLogsByDateRange(startDate, endDate, pageable);

        return ResponseEntity.ok(ApiResponse.success(
            logs,
            "Logs no período obtidos com sucesso"
        ));
    }

    // ===========================================
    // LOGS DE SEGURANÇA
    // ===========================================

    /**
     * Lista logs de tentativas de acesso negado
     * 
     * GET /api/admin/audit/security/denied?page=0&size=20
     * 
     * @param page Número da página
     * @param size Tamanho da página
     * @param startDate Data inicial (opcional)
     * @param endDate Data final (opcional)
     * @return Página de logs de acesso negado
     */
    @GetMapping("/security/denied")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAccessDeniedLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Listando logs de acesso negado (período: {} a {})", startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLogResponse> logs = auditService.findAccessDeniedLogs(startDate, endDate, pageable);

        return ResponseEntity.ok(ApiResponse.success(
            logs,
            "Logs de acesso negado obtidos com sucesso"
        ));
    }

    /**
     * Lista todos os logs de segurança em um período
     * 
     * GET /api/admin/audit/security/all?startDate=2025-11-01&endDate=2025-11-30
     * 
     * @param startDate Data inicial (obrigatório)
     * @param endDate Data final (obrigatório)
     * @param page Número da página
     * @param size Tamanho da página
     * @return Página de logs de segurança
     */
    @GetMapping("/security/all")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllSecurityLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Listando todos os logs de segurança (período: {} a {})", startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLogResponse> logs = auditService.findSecurityLogs(startDate, endDate, pageable);

        return ResponseEntity.ok(ApiResponse.success(
            logs,
            "Logs de segurança obtidos com sucesso"
        ));
    }

    // ===========================================
    // ESTATÍSTICAS
    // ===========================================

    /**
     * Obtém estatísticas de auditoria em um período
     * 
     * GET /api/admin/audit/stats?startDate=2025-11-01&endDate=2025-11-30
     * 
     * @param startDate Data inicial (obrigatório)
     * @param endDate Data final (obrigatório)
     * @return Estatísticas calculadas
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AuditStatsResponse>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Se não fornecido, usar últimos 30 dias
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        log.info("Calculando estatísticas de auditoria (período: {} a {})", startDate, endDate);

        AuditStatsResponse stats = auditService.getStatistics(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(
            stats,
            "Estatísticas de auditoria calculadas com sucesso"
        ));
    }

    // ===========================================
    // EXPORTAÇÃO
    // ===========================================

    /**
     * Exporta logs para CSV
     * 
     * GET /api/admin/audit/export/csv?startDate=2025-11-01&endDate=2025-11-30
     * 
     * @param startDate Data inicial (obrigatório)
     * @param endDate Data final (obrigatório)
     * @param eventType Tipo de evento (opcional)
     * @return Arquivo CSV para download
     */
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportToCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AuditEventType eventType
    ) {
        log.info("Exportando logs para CSV (período: {} a {}, tipo: {})", 
                startDate, endDate, eventType);

        String csv = auditService.exportToCsv(startDate, endDate, eventType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", 
            "audit_logs_" + startDate + "_" + endDate + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }

    /**
     * Exporta logs para JSON
     * 
     * GET /api/admin/audit/export/json?startDate=2025-11-01&endDate=2025-11-30
     * 
     * @param startDate Data inicial (obrigatório)
     * @param endDate Data final (obrigatório)
     * @param eventType Tipo de evento (opcional)
     * @param page Número da página
     * @param size Tamanho da página
     * @return Logs em JSON
     */
    @GetMapping("/export/json")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> exportToJson(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AuditEventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        log.info("Exportando logs para JSON (período: {} a {}, tipo: {})", 
                startDate, endDate, eventType);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLogResponse> logs;

        if (eventType != null) {
            logs = auditService.findLogsByEventTypeAndDateRange(eventType, startDate, endDate, pageable);
        } else {
            logs = auditService.findLogsByDateRange(startDate, endDate, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(
            logs,
            "Logs exportados para JSON com sucesso"
        ));
    }

    // ===========================================
    // HEALTH CHECK
    // ===========================================

    /**
     * Health check do serviço de auditoria
     * 
     * GET /api/admin/audit/health
     * 
     * @return Status do serviço
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Object>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success(
            Map.of(
                "status", "UP",
                "service", "audit",
                "version", "4.0"
            ),
            "Serviço de auditoria operacional"
        ));
    }
}

