# 📊 FASE 4 - AUDIT LOGGING AVANÇADO - Especificação Técnica

**Data de Início:** 12 de Novembro de 2025  
**Versão:** 4.0  
**Status:** 🚧 EM IMPLEMENTAÇÃO  
**Prioridade:** MÉDIA  
**Estimativa:** 1-2 semanas

---

## 📋 ÍNDICE

1. [Visão Geral](#visão-geral)
2. [Objetivos](#objetivos)
3. [Arquitetura](#arquitetura)
4. [Modelo de Dados](#modelo-de-dados)
5. [Endpoints](#endpoints)
6. [Implementação](#implementação)
7. [Testes](#testes)
8. [Documentação](#documentação)

---

## 🎯 VISÃO GERAL

A Fase 4 implementa um **sistema completo de auditoria** para rastrear todas as ações críticas no sistema Neuroefficiency, com foco especial em operações RBAC (Roles, Permissions, Pacotes).

### **Problema a Resolver:**

Atualmente, o sistema:
- ✅ Tem auditoria básica para password reset (`PasswordResetAudit`)
- ❌ Não rastreia mudanças em roles e permissions
- ❌ Não registra tentativas de acesso não autorizado
- ❌ Não oferece relatórios de compliance
- ❌ Dificulta investigação de problemas de segurança

### **Solução:**

Sistema de auditoria abrangente que:
- ✅ Registra TODAS as ações críticas automaticamente
- ✅ Fornece endpoints de consulta e relatórios
- ✅ Exporta dados para compliance (CSV, JSON)
- ✅ Facilita debugging e investigação
- ✅ Atende requisitos LGPD

---

## 🎯 OBJETIVOS

### **Objetivos Funcionais:**

1. ✅ **Rastreabilidade Total**
   - Registrar quem fez, o que fez, quando e de onde

2. ✅ **Compliance LGPD**
   - Histórico completo de ações sobre dados sensíveis
   - Exportação de relatórios para auditoria

3. ✅ **Segurança**
   - Detecção de tentativas de acesso não autorizado
   - Identificação de padrões suspeitos

4. ✅ **Governança**
   - Relatórios gerenciais de uso do sistema
   - Estatísticas de ações por período

### **Objetivos Técnicos:**

1. ✅ **Performance**
   - Auditoria assíncrona (não bloquear operações)
   - Índices otimizados para queries

2. ✅ **Escalabilidade**
   - Preparado para milhões de registros
   - Paginação eficiente
   - Possibilidade de arquivo/limpeza

3. ✅ **Extensibilidade**
   - Fácil adicionar novos tipos de eventos
   - Suporte a metadados customizados (JSON)

---

## 🏗️ ARQUITETURA

### **Componentes:**

```
┌─────────────────────────────────────────────────────────┐
│                    CAMADA CONTROLLER                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ RbacController│  │AuditController│  │AuthController│  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
└─────────┼──────────────────┼──────────────────┼─────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────┐
│                     CAMADA SERVICE                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  RbacService  │  │ AuditService │  │  AuthService │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
└─────────┼──────────────────┼──────────────────┼─────────┘
          │                  │                  │
          │         ┌────────▼────────┐         │
          │         │  AuditAspect    │         │
          │         │  (@Auditable)   │         │
          │         └─────────────────┘         │
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────┐
│                   CAMADA REPOSITORY                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │RoleRepository │  │AuditLogRepo  │  │UsuarioRepo   │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
└─────────┼──────────────────┼──────────────────┼─────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────┐
│                   BANCO DE DADOS (H2/PostgreSQL)        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │    roles     │  │  audit_logs  │  │   usuarios   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### **Fluxo de Auditoria:**

```
1. Usuário executa ação (ex: criar role)
          ↓
2. Controller recebe request
          ↓
3. Service executa operação
          ↓
4. AuditService.log() é chamado (dentro da transação)
          ↓
5. Registro salvo em audit_logs
          ↓
6. Response retornado ao usuário
```

---

## 📊 MODELO DE DADOS

### **Entidade: AuditLog**

```java
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_event_type", columnList = "event_type"),
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_user_timestamp", columnList = "user_id,timestamp")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AuditEventType eventType;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "username", length = 50)
    private String username;
    
    @Column(name = "target_id", length = 100)
    private String targetId;
    
    @Column(name = "target_type", length = 50)
    private String targetType;
    
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // JSON com metadados
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 255)
    private String userAgent;
    
    @Column(name = "success", nullable = false)
    private Boolean success;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
```

### **Enum: AuditEventType**

```java
public enum AuditEventType {
    // Autenticação
    AUTH_LOGIN,
    AUTH_LOGOUT,
    AUTH_FAILED_LOGIN,
    AUTH_REGISTER,
    AUTH_PASSWORD_CHANGE,
    
    // RBAC - Roles
    RBAC_ROLE_CREATED,
    RBAC_ROLE_UPDATED,
    RBAC_ROLE_DELETED,
    RBAC_ROLE_ASSIGNED,
    RBAC_ROLE_REMOVED,
    
    // RBAC - Permissions
    RBAC_PERMISSION_CREATED,
    RBAC_PERMISSION_UPDATED,
    RBAC_PERMISSION_DELETED,
    RBAC_PERMISSION_ADDED_TO_ROLE,
    RBAC_PERMISSION_REMOVED_FROM_ROLE,
    
    // RBAC - Pacotes
    RBAC_PACKAGE_CREATED,
    RBAC_PACKAGE_UPDATED,
    RBAC_PACKAGE_DELETED,
    RBAC_PACKAGE_EXPIRED,
    
    // Segurança
    SECURITY_ACCESS_DENIED,
    SECURITY_UNAUTHORIZED_ATTEMPT,
    SECURITY_SUSPICIOUS_ACTIVITY,
    
    // Sistema
    SYSTEM_CONFIG_CHANGED,
    SYSTEM_ERROR
}
```

### **Migration Flyway V6:**

```sql
-- V6__create_audit_logs_table.sql

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    user_id BIGINT,
    username VARCHAR(50),
    target_id VARCHAR(100),
    target_type VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) 
        REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Índices para performance
CREATE INDEX idx_audit_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_user_timestamp ON audit_logs(user_id, timestamp);
CREATE INDEX idx_audit_success ON audit_logs(success);
```

---

## 🌐 ENDPOINTS

### **Grupo: Admin Audit** (Protegidos com @PreAuthorize("hasRole('ADMIN')"))

#### **1. Listar Logs (Paginado)**
```
GET /api/admin/audit/logs
Query Params:
  - page: int (default: 0)
  - size: int (default: 20)
  - eventType: AuditEventType (opcional)
  - userId: Long (opcional)
  - success: Boolean (opcional)
  - startDate: LocalDate (opcional)
  - endDate: LocalDate (opcional)
  - sort: string (default: "timestamp,desc")

Response: Page<AuditLogResponse>
```

#### **2. Obter Log por ID**
```
GET /api/admin/audit/logs/{id}

Response: AuditLogResponse
```

#### **3. Listar Logs de Usuário**
```
GET /api/admin/audit/logs/user/{userId}
Query Params:
  - startDate: LocalDate (opcional)
  - endDate: LocalDate (opcional)
  - page: int (default: 0)
  - size: int (default: 20)

Response: Page<AuditLogResponse>
```

#### **4. Listar Logs por Tipo de Evento**
```
GET /api/admin/audit/logs/type/{eventType}
Query Params:
  - startDate: LocalDate (opcional)
  - endDate: LocalDate (opcional)
  - page: int (default: 0)
  - size: int (default: 20)

Response: Page<AuditLogResponse>
```

#### **5. Estatísticas de Auditoria**
```
GET /api/admin/audit/stats
Query Params:
  - startDate: LocalDate (opcional)
  - endDate: LocalDate (opcional)

Response: AuditStatsResponse {
  totalEvents: Long,
  eventsByType: Map<AuditEventType, Long>,
  topUsers: List<UserActivityStats>,
  successRate: Double,
  failedAttempts: Long,
  period: DateRange
}
```

#### **6. Logs de Tentativas de Acesso Negado**
```
GET /api/admin/audit/security/denied
Query Params:
  - startDate: LocalDate (opcional)
  - endDate: LocalDate (opcional)
  - page: int (default: 0)
  - size: int (default: 20)

Response: Page<AuditLogResponse>
```

#### **7. Exportar Logs para CSV**
```
GET /api/admin/audit/export/csv
Query Params:
  - startDate: LocalDate (obrigatório)
  - endDate: LocalDate (obrigatório)
  - eventType: AuditEventType (opcional)

Response: text/csv (download)
```

#### **8. Exportar Logs para JSON**
```
GET /api/admin/audit/export/json
Query Params:
  - startDate: LocalDate (obrigatório)
  - endDate: LocalDate (obrigatório)
  - eventType: AuditEventType (opcional)

Response: application/json (download)
```

---

## 🔧 IMPLEMENTAÇÃO

### **Sprint 1: Fundação (3-4 dias)**

#### **Dia 1-2:**
1. ✅ Criar `AuditEventType` enum
2. ✅ Criar `AuditLog` entity
3. ✅ Criar `AuditLogRepository`
4. ✅ Criar migration V6
5. ✅ Testes unitários básicos

#### **Dia 3-4:**
6. ✅ Implementar `AuditService`
7. ✅ Criar DTOs (AuditLogResponse, AuditStatsResponse)
8. ✅ Testes unitários do service
9. ✅ Integração com RbacService (log de ações)

### **Sprint 2: Endpoints (3-4 dias)**

#### **Dia 5-6:**
10. ✅ Criar `AuditController`
11. ✅ Implementar endpoints 1-4 (listagens)
12. ✅ Testes de integração

#### **Dia 7-8:**
13. ✅ Implementar endpoint 5 (estatísticas)
14. ✅ Implementar endpoints 6-8 (segurança + export)
15. ✅ Testes E2E

### **Sprint 3: Refinamento (2-3 dias)**

#### **Dia 9-10:**
16. ✅ Implementar `@Auditable` annotation
17. ✅ Implementar `AuditAspect` (AOP)
18. ✅ Integrar com todos os endpoints críticos

#### **Dia 11:**
19. ✅ Atualizar Collection Postman v4.0
20. ✅ Atualizar documentação
21. ✅ Review final e ajustes

---

## 🧪 TESTES

### **Testes Unitários:**

```java
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {
    
    @Test
    void testLogRoleCreation() { }
    
    @Test
    void testLogPermissionAdded() { }
    
    @Test
    void testLogAccessDenied() { }
    
    @Test
    void testFindLogsByUser() { }
    
    @Test
    void testGetStatistics() { }
    
    @Test
    void testExportToCsv() { }
}
```

### **Testes de Integração:**

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuditControllerIntegrationTest {
    
    @Test
    void testGetAllLogsAsAdmin() { }
    
    @Test
    void testGetAllLogsAsNonAdmin_ShouldReturn403() { }
    
    @Test
    void testGetLogsByUser() { }
    
    @Test
    void testGetStatistics() { }
    
    @Test
    void testExportCsv() { }
}
```

### **Cobertura Esperada:**
- ✅ Service: 90%+
- ✅ Controller: 85%+
- ✅ Repository: Custom queries testadas

---

## 📚 DOCUMENTAÇÃO

### **Arquivos a Criar/Atualizar:**

1. ✅ **Este arquivo** - Especificação técnica completa
2. ✅ **README.md** - Atualizar para Fase 4
3. ✅ **DOCS/GUIA_TÉCNICO_COMPLETO.md** - Adicionar seção Auditoria
4. ✅ **DOCS/GUIA_POSTMAN.md** - Documentar novos endpoints
5. ✅ **DOCS/CHANGELOG.md** - Registrar versão 4.0.0
6. ✅ **Collection Postman v4.0** - Adicionar 8 novos endpoints

### **Guias Adicionais:**

7. ✅ **DOCS/GUIA_AUDITORIA.md** - Guia de uso do sistema de auditoria
8. ✅ **DOCS/COMPLIANCE_LGPD.md** - Como usar auditoria para compliance

---

## 🎯 CRITÉRIOS DE ACEITAÇÃO

### **Funcional:**
- [ ] Todos os eventos RBAC são auditados automaticamente
- [ ] Tentativas de acesso negado são registradas
- [ ] Endpoints de consulta funcionam com paginação
- [ ] Estatísticas são calculadas corretamente
- [ ] Exportação CSV/JSON funciona

### **Técnico:**
- [ ] Todos os testes passando (100%)
- [ ] Cobertura de testes > 85%
- [ ] Performance: auditoria não adiciona > 50ms às operações
- [ ] Migration Flyway executa sem erros
- [ ] Código segue padrões do projeto

### **Documentação:**
- [ ] Collection Postman v4.0 completa
- [ ] README atualizado
- [ ] Guias técnicos atualizados
- [ ] Changelog atualizado

---

## 📊 MÉTRICAS DE SUCESSO

- ✅ **35 endpoints** operacionais (27 atuais + 8 novos)
- ✅ **100% das ações RBAC** auditadas
- ✅ **Zero impacto** na performance (< 50ms overhead)
- ✅ **Relatórios de compliance** disponíveis
- ✅ **Sistema pronto para produção**

---

**Próximo Passo:** Implementar Sprint 1 - Fundação  
**Responsável:** Equipe Neuroefficiency  
**Data Prevista de Conclusão:** 26 de Novembro de 2025

---

**Documento preparado por:** AI Assistant (Senior Software Engineer)  
**Data:** 12 de Novembro de 2025  
**Versão:** 1.0  
**Status:** ✅ Especificação Aprovada - Pronto para Implementação

