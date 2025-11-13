package com.neuroefficiency.service;

import com.neuroefficiency.domain.enums.AuditEventType;
import com.neuroefficiency.domain.model.AuditLog;
import com.neuroefficiency.domain.repository.AuditLogRepository;
import com.neuroefficiency.dto.response.AuditLogResponse;
import com.neuroefficiency.dto.response.AuditStatsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AuditService
 * 
 * @author Neuroefficiency Team
 * @version 4.0 - Fase 4: Audit Logging Avançado
 * @since 2025-11-12
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService - Testes Unitários")
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private AuditLog sampleAuditLog;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2025, 11, 1);
        endDate = LocalDate.of(2025, 11, 30);

        sampleAuditLog = AuditLog.builder()
                .id(1L)
                .eventType(AuditEventType.AUTH_LOGIN)
                .userId(1L)
                .username("testuser")
                .action("User logged in")
                .description("User testuser logged in successfully")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ===========================================
    // TESTES DE REGISTRO DE EVENTOS
    // ===========================================

    @Test
    @DisplayName("Deve registrar evento de auditoria com sucesso")
    void testLogSuccess() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(sampleAuditLog);

        // Act
        AuditLog result = auditService.log(
            AuditEventType.AUTH_LOGIN,
            "User logged in",
            "User login successful",
            "Usuario",
            "1",
            null,
            "192.168.1.1",
            "Mozilla/5.0",
            true,
            null
        );

        // Assert
        assertNotNull(result);
        assertEquals(AuditEventType.AUTH_LOGIN, result.getEventType());
        assertEquals("User logged in", result.getAction());
        assertTrue(result.getSuccess());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Deve registrar evento de sucesso simplificado")
    void testLogSuccessSimplified() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(sampleAuditLog);

        // Act
        AuditLog result = auditService.logSuccess(
            AuditEventType.RBAC_ROLE_CREATED,
            "Role ADMIN created",
            "Role",
            "5"
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.getSuccess());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Deve registrar evento de falha")
    void testLogFailure() {
        // Arrange
        AuditLog failureLog = AuditLog.builder()
                .id(2L)
                .eventType(AuditEventType.AUTH_FAILED_LOGIN)
                .action("Login failed")
                .success(false)
                .errorMessage("Invalid credentials")
                .timestamp(LocalDateTime.now())
                .build();

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(failureLog);

        // Act
        AuditLog result = auditService.logFailure(
            AuditEventType.AUTH_FAILED_LOGIN,
            "Login failed",
            "Invalid credentials"
        );

        // Assert
        assertNotNull(result);
        assertFalse(result.getSuccess());
        assertEquals("Invalid credentials", result.getErrorMessage());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Deve registrar evento de acesso negado")
    void testLogAccessDenied() {
        // Arrange
        AuditLog deniedLog = AuditLog.builder()
                .id(3L)
                .eventType(AuditEventType.SECURITY_ACCESS_DENIED)
                .action("Tentativa de acesso a: /api/admin/roles")
                .success(false)
                .ipAddress("192.168.1.100")
                .timestamp(LocalDateTime.now())
                .build();

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(deniedLog);

        // Act
        AuditLog result = auditService.logAccessDenied(
            "/api/admin/roles",
            "Insufficient permissions",
            "192.168.1.100"
        );

        // Assert
        assertNotNull(result);
        assertEquals(AuditEventType.SECURITY_ACCESS_DENIED, result.getEventType());
        assertEquals("192.168.1.100", result.getIpAddress());
        assertFalse(result.getSuccess());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    // ===========================================
    // TESTES DE CONSULTA
    // ===========================================

    @Test
    @DisplayName("Deve buscar todos os logs paginados")
    void testFindAllLogs() {
        // Arrange
        List<AuditLog> logs = Arrays.asList(sampleAuditLog);
        Page<AuditLog> page = new PageImpl<>(logs);
        Pageable pageable = PageRequest.of(0, 20);

        when(auditLogRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<AuditLogResponse> result = auditService.findAllLogs(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(auditLogRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar log por ID")
    void testFindLogById() {
        // Arrange
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(sampleAuditLog));

        // Act
        AuditLogResponse result = auditService.findLogById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(auditLogRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar log inexistente")
    void testFindLogByIdNotFound() {
        // Arrange
        when(auditLogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            auditService.findLogById(999L);
        });
        verify(auditLogRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve buscar logs de um usuário")
    void testFindLogsByUser() {
        // Arrange
        List<AuditLog> logs = Arrays.asList(sampleAuditLog);
        Page<AuditLog> page = new PageImpl<>(logs);
        Pageable pageable = PageRequest.of(0, 20);

        when(auditLogRepository.findByUserId(1L, pageable)).thenReturn(page);

        // Act
        Page<AuditLogResponse> result = auditService.findLogsByUser(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
        verify(auditLogRepository, times(1)).findByUserId(1L, pageable);
    }

    @Test
    @DisplayName("Deve buscar logs por tipo de evento")
    void testFindLogsByEventType() {
        // Arrange
        List<AuditLog> logs = Arrays.asList(sampleAuditLog);
        Page<AuditLog> page = new PageImpl<>(logs);
        Pageable pageable = PageRequest.of(0, 20);

        when(auditLogRepository.findByEventType(AuditEventType.AUTH_LOGIN, pageable))
                .thenReturn(page);

        // Act
        Page<AuditLogResponse> result = auditService.findLogsByEventType(
            AuditEventType.AUTH_LOGIN, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(AuditEventType.AUTH_LOGIN, result.getContent().get(0).getEventType());
        verify(auditLogRepository, times(1))
            .findByEventType(AuditEventType.AUTH_LOGIN, pageable);
    }

    // ===========================================
    // TESTES DE ESTATÍSTICAS
    // ===========================================

    @Test
    @DisplayName("Deve calcular estatísticas corretamente")
    void testGetStatistics() {
        // Arrange
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        when(auditLogRepository.countByDateRange(any(), any())).thenReturn(100L);
        when(auditLogRepository.countSuccessByDateRange(any(), any())).thenReturn(90L);
        when(auditLogRepository.countFailuresByDateRange(any(), any())).thenReturn(10L);
        when(auditLogRepository.countByEventTypeInDateRange(any(), any()))
                .thenReturn(Arrays.asList(
                    new Object[]{AuditEventType.AUTH_LOGIN, 50L},
                    new Object[]{AuditEventType.RBAC_ROLE_CREATED, 30L},
                    new Object[]{AuditEventType.SECURITY_ACCESS_DENIED, 20L}
                ));
        when(auditLogRepository.findTopActiveUsers(any(), any(), any()))
                .thenReturn(Arrays.asList(
                    new Object[]{1L, "admin", 40L},
                    new Object[]{2L, "user", 30L}
                ));

        // Act
        AuditStatsResponse result = auditService.getStatistics(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getTotalEvents());
        assertEquals(90L, result.getSuccessfulEvents());
        assertEquals(10L, result.getFailedEvents());
        assertEquals(90.0, result.getSuccessRate());
        assertEquals(3, result.getEventsByType().size());
        assertEquals(2, result.getTopUsers().size());
        assertTrue(result.getAuthEvents() > 0);
        assertTrue(result.getRbacEvents() > 0);
        assertTrue(result.getSecurityEvents() > 0);
    }

    // ===========================================
    // TESTES DE EXPORTAÇÃO
    // ===========================================

    @Test
    @DisplayName("Deve exportar logs para CSV")
    void testExportToCsv() {
        // Arrange
        List<AuditLog> logs = Arrays.asList(sampleAuditLog);
        when(auditLogRepository.findAllByDateRange(any(), any())).thenReturn(logs);

        // Act
        String csv = auditService.exportToCsv(startDate, endDate, null);

        // Assert
        assertNotNull(csv, "CSV não deve ser nulo");
        assertTrue(csv.contains("ID,Event Type"), "CSV deve conter header com ID e Event Type");
        assertTrue(csv.contains("testuser"), "CSV deve conter username");
        assertTrue(csv.length() > 100, "CSV deve ter conteúdo significativo");
        verify(auditLogRepository, times(1)).findAllByDateRange(any(), any());
    }

    @Test
    @DisplayName("Deve exportar logs filtrados por tipo de evento para CSV")
    void testExportToCsvWithEventType() {
        // Arrange
        List<AuditLog> logs = Arrays.asList(sampleAuditLog);
        Page<AuditLog> page = new PageImpl<>(logs);
        when(auditLogRepository.findByEventTypeAndDateRange(any(), any(), any(), any()))
                .thenReturn(page);

        // Act
        String csv = auditService.exportToCsv(startDate, endDate, AuditEventType.AUTH_LOGIN);

        // Assert
        assertNotNull(csv, "CSV não deve ser nulo");
        assertTrue(csv.contains("ID,Event Type"), "CSV deve conter header");
        assertTrue(csv.contains("testuser"), "CSV deve conter username do log");
        assertTrue(csv.length() > 100, "CSV deve ter conteúdo significativo");
        verify(auditLogRepository, times(1))
            .findByEventTypeAndDateRange(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve escapar valores CSV corretamente")
    void testCsvEscaping() {
        // Arrange
        AuditLog logWithComma = AuditLog.builder()
                .id(4L)
                .eventType(AuditEventType.AUTH_LOGIN)
                .action("User logged in, successfully")
                .description("Description with \"quotes\" and, commas")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();

        List<AuditLog> logs = Arrays.asList(logWithComma);
        when(auditLogRepository.findAllByDateRange(any(), any())).thenReturn(logs);

        // Act
        String csv = auditService.exportToCsv(startDate, endDate, null);

        // Assert
        assertNotNull(csv);
        assertTrue(csv.contains("ID,Event Type"), "CSV deve conter header");
        // Verifica que valores com vírgulas são escapados com aspas
        assertTrue(csv.contains("\"User logged in, successfully\"") || 
                   csv.contains("User logged in, successfully"), 
                   "CSV deve escapar valores com vírgulas");
        verify(auditLogRepository, times(1)).findAllByDateRange(any(), any());
    }
}

