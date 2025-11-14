package com.neuroefficiency.controller;

import com.neuroefficiency.domain.enums.AuditEventType;
import com.neuroefficiency.domain.model.AuditLog;
import com.neuroefficiency.domain.model.Role;
import com.neuroefficiency.domain.model.Usuario;
import com.neuroefficiency.domain.repository.AuditLogRepository;
import com.neuroefficiency.domain.repository.RoleRepository;
import com.neuroefficiency.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para AuditController
 * 
 * @author Neuroefficiency Team
 * @version 4.0 - Fase 4: Audit Logging Avançado
 * @since 2025-11-12
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AuditController - Testes de Integração")
class AuditControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario adminUser;
    private Usuario normalUser;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Limpar dados de auditoria primeiro
        auditLogRepository.deleteAll();
        
        // Limpar usuários (isso remove as associações com roles)
        usuarioRepository.deleteAll();
        
        // Limpar roles
        roleRepository.deleteAll();

        // Criar role ADMIN ou buscar se já existe
        adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name("ADMIN")
                            .description("Administrator")
                            .active(true)
                            .build();
                    return roleRepository.save(newRole);
                });

        // Criar role USER para usuário normal
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name("USER")
                            .description("Regular User")
                            .active(true)
                            .build();
                    return roleRepository.save(newRole);
                });

        // Criar usuário admin
        adminUser = Usuario.builder()
                .username("admin")
                .email("admin@test.com")
                .passwordHash(passwordEncoder.encode("Admin@1234"))
                .enabled(true)
                .build();
        adminUser.getRoles().add(adminRole);
        adminUser = usuarioRepository.save(adminUser);

        // Criar usuário normal com role USER (autenticado mas sem permissão de admin)
        normalUser = Usuario.builder()
                .username("normaluser")
                .email("user@test.com")
                .passwordHash(passwordEncoder.encode("User@1234"))
                .enabled(true)
                .build();
        normalUser.getRoles().add(userRole);
        normalUser = usuarioRepository.save(normalUser);

        // Criar alguns logs de auditoria para testes
        createSampleAuditLogs();
    }

    private void createSampleAuditLogs() {
        LocalDateTime now = LocalDateTime.now();

        // Log 1: Login de admin
        AuditLog log1 = AuditLog.builder()
                .eventType(AuditEventType.AUTH_LOGIN)
                .userId(adminUser.getId())
                .username(adminUser.getUsername())
                .action("Admin logged in")
                .description("Admin user logged in successfully")
                .ipAddress("192.168.1.1")
                .success(true)
                .timestamp(now.minusHours(2))
                .build();
        auditLogRepository.save(log1);

        // Log 2: Tentativa de login falha
        AuditLog log2 = AuditLog.builder()
                .eventType(AuditEventType.AUTH_FAILED_LOGIN)
                .username("hacker")
                .action("Failed login attempt")
                .description("Invalid credentials")
                .ipAddress("192.168.1.100")
                .success(false)
                .errorMessage("Invalid username or password")
                .timestamp(now.minusHours(1))
                .build();
        auditLogRepository.save(log2);

        // Log 3: Role criada
        AuditLog log3 = AuditLog.builder()
                .eventType(AuditEventType.RBAC_ROLE_CREATED)
                .userId(adminUser.getId())
                .username(adminUser.getUsername())
                .action("Created role MANAGER")
                .targetType("Role")
                .targetId("5")
                .success(true)
                .timestamp(now.minusMinutes(30))
                .build();
        auditLogRepository.save(log3);

        // Log 4: Acesso negado
        AuditLog log4 = AuditLog.builder()
                .eventType(AuditEventType.SECURITY_ACCESS_DENIED)
                .userId(normalUser.getId())
                .username(normalUser.getUsername())
                .action("Attempted access to /api/admin/roles")
                .description("Insufficient permissions")
                .ipAddress("192.168.1.50")
                .success(false)
                .errorMessage("Access denied")
                .timestamp(now.minusMinutes(10))
                .build();
        auditLogRepository.save(log4);
    }

    // ===========================================
    // TESTES DE CONSULTA DE LOGS
    // ===========================================

    @Test
    @DisplayName("Admin deve conseguir listar todos os logs")
    void testGetAllLogsAsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs")
                        .with(user(adminUser))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("Usuário sem role ADMIN não deve conseguir listar logs")
    void testGetAllLogsAsNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs")
                        .with(user(normalUser))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve conseguir buscar log por ID")
    void testGetLogById() throws Exception {
        AuditLog log = auditLogRepository.findAll().get(0);

        mockMvc.perform(get("/api/admin/audit/logs/" + log.getId())
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(log.getId()))
                .andExpect(jsonPath("$.data.username").value(log.getUsername()))
                .andExpect(jsonPath("$.data.action").value(log.getAction()));
    }

    @Test
    @DisplayName("Deve conseguir buscar logs de um usuário específico")
    void testGetLogsByUser() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs/user/" + adminUser.getId())
                        .with(user(adminUser))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Deve conseguir buscar logs por tipo de evento")
    void testGetLogsByEventType() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs/type/AUTH_LOGIN")
                        .with(user(adminUser))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].eventType", everyItem(is("AUTH_LOGIN"))));
    }

    @Test
    @DisplayName("Deve conseguir buscar logs por período")
    void testGetLogsByDateRange() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        mockMvc.perform(get("/api/admin/audit/logs/date-range")
                        .with(user(adminUser))
                        .param("startDate", yesterday.toString())
                        .param("endDate", today.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ===========================================
    // TESTES DE LOGS DE SEGURANÇA
    // ===========================================

    @Test
    @DisplayName("Deve conseguir buscar logs de acesso negado")
    void testGetAccessDeniedLogs() throws Exception {
        mockMvc.perform(get("/api/admin/audit/security/denied")
                        .with(user(adminUser))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[*].eventType", 
                          everyItem(is("SECURITY_ACCESS_DENIED"))));
    }

    @Test
    @DisplayName("Deve conseguir buscar todos os logs de segurança")
    void testGetAllSecurityLogs() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        mockMvc.perform(get("/api/admin/audit/security/all")
                        .with(user(adminUser))
                        .param("startDate", yesterday.toString())
                        .param("endDate", today.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ===========================================
    // TESTES DE ESTATÍSTICAS
    // ===========================================

    @Test
    @DisplayName("Deve conseguir obter estatísticas de auditoria")
    void testGetStatistics() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        mockMvc.perform(get("/api/admin/audit/stats")
                        .with(user(adminUser))
                        .param("startDate", yesterday.toString())
                        .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEvents").isNumber())
                .andExpect(jsonPath("$.data.successfulEvents").isNumber())
                .andExpect(jsonPath("$.data.failedEvents").isNumber())
                .andExpect(jsonPath("$.data.successRate").isNumber())
                .andExpect(jsonPath("$.data.eventsByType").isMap())
                .andExpect(jsonPath("$.data.topUsers").isArray());
    }

    // ===========================================
    // TESTES DE EXPORTAÇÃO
    // ===========================================

    @Test
    @DisplayName("Deve conseguir exportar logs para CSV")
    void testExportToCsv() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        mockMvc.perform(get("/api/admin/audit/export/csv")
                        .with(user(adminUser))
                        .param("startDate", yesterday.toString())
                        .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", 
                          containsString("attachment")))
                .andExpect(content().string(containsString("ID,Event Type")));
    }

    @Test
    @DisplayName("Deve conseguir exportar logs para JSON")
    void testExportToJson() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        mockMvc.perform(get("/api/admin/audit/export/json")
                        .with(user(adminUser))
                        .param("startDate", yesterday.toString())
                        .param("endDate", today.toString())
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ===========================================
    // TESTE DE HEALTH CHECK
    // ===========================================

    @Test
    @DisplayName("Health check deve retornar status UP")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/admin/audit/health")
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("audit"))
                .andExpect(jsonPath("$.data.version").value("4.0"));
    }

    // ===========================================
    // TESTES DE PAGINAÇÃO E ORDENAÇÃO
    // ===========================================

    @Test
    @DisplayName("Deve respeitar parâmetros de paginação")
    void testPaginationParameters() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs")
                        .with(user(adminUser))
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(lessThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.number").value(0));
    }

    @Test
    @DisplayName("Deve respeitar parâmetro de ordenação")
    void testSortParameter() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs")
                        .with(user(adminUser))
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "timestamp,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}

