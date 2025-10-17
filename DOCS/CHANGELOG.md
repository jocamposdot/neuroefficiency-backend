# 📝 CHANGELOG
## Histórico de Versões do Neuroefficiency Backend

Todas as mudanças notáveis do projeto serão documentadas neste arquivo.

Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).

---

## [3.1.0] - 2025-10-17

### 🔧 Correção Crítica: LazyInitializationException

#### 🐛 Corrigido
- **LazyInitializationException** em 8 endpoints RBAC
  - **Endpoints corrigidos:**
    - `GET /api/admin/rbac/roles`
    - `GET /api/admin/rbac/permissions`
    - `GET /api/admin/rbac/users/admin`
    - `GET /api/admin/rbac/users/clinico`
    - `GET /api/admin/rbac/users/role/{roleName}`
    - `POST /api/admin/rbac/roles`
    - `POST /api/admin/rbac/permissions`
    - `POST /api/admin/rbac/roles/{roleName}/permissions/{permissionName}`
  - **Problema:** Controller retornava entidades JPA diretamente, causando erro ao serializar collections lazy após fechamento da sessão Hibernate
  - **Solução:** Implementado padrão DTO (Data Transfer Object) para todas as respostas RBAC

#### ✨ Adicionado
- **DTOs de Response:**
  - `RoleResponse` - DTO para Role com dois métodos de conversão:
    - `fromEntity()` - Sem permissions (para listagens)
    - `fromEntityWithPermissions()` - Com permissions (para detalhes)
  - `PermissionResponse` - DTO para Permission com dois métodos de conversão:
    - `fromEntity()` - Sem roles (para listagens)
    - `fromEntityWithRoles()` - Com roles (para detalhes)

- **Documentação:**
  - `DOCS/ANALISE-ERRO-LAZY-INITIALIZATION.md` - Análise técnica profunda do erro, causas, soluções possíveis e implementação

#### 🔄 Modificado
- **`RbacController` - 8 endpoints atualizados:**
  - `getAllRoles()` → `List<RoleResponse>`
  - `getAllRolesWithPermissions()` → `List<RoleResponse>` (com permissions)
  - `getAllPermissions()` → `List<PermissionResponse>`
  - `getPermissionsByResource()` → `List<PermissionResponse>`
  - `getUsersByRole()` → `List<UserResponse>`
  - `getAdminUsers()` → `List<UserResponse>`
  - `getClinicoUsers()` → `List<UserResponse>`
  - `createRole()` → `RoleResponse`
  - `createPermission()` → `PermissionResponse`
  - `addPermissionToRole()` → `RoleResponse` (com permissions)
  - `removePermissionFromRole()` → `RoleResponse` (com permissions)

#### ✅ Testes
- **Todos os 47 testes continuam passando (100%)**
  - 16 testes unitários `RbacService`
  - 15 testes integração `RbacController`
  - 9 testes integração `AuthController`
  - 6 testes unitários `AuthenticationService`
  - 1 teste contexto Spring Boot

#### 📊 Benefícios
- ✅ Arquitetura limpa (separação Domain vs Presentation)
- ✅ Performance otimizada (carrega só o necessário)
- ✅ Segurança melhorada (controle sobre dados expostos)
- ✅ Evita serialização circular
- ✅ Facilita evolução e versionamento da API
- ✅ Flexibilidade (endpoints com/sem associations)

---

## [3.0.0] - 2025-10-16

### 🎉 Fase 3: RBAC (Role-Based Access Control) - COMPLETA

#### ✨ Adicionado
- **15 novos endpoints REST ADMIN:**
  - `GET /api/admin/rbac/roles` - Listar roles
  - `POST /api/admin/rbac/roles` - Criar role
  - `GET /api/admin/rbac/permissions` - Listar permissões
  - `POST /api/admin/rbac/permissions` - Criar permissão
  - `GET /api/admin/rbac/stats` - Estatísticas RBAC
  - `GET /api/admin/rbac/users/admin` - Listar usuários ADMIN
  - `GET /api/admin/rbac/users/clinico` - Listar usuários CLINICO
  - `POST /api/admin/rbac/users/{id}/roles/{roleName}` - Atribuir role
  - `DELETE /api/admin/rbac/users/{id}/roles/{roleName}` - Remover role
  - `GET /api/admin/rbac/users/{id}/has-role/{roleName}` - Verificar role
  - `GET /api/admin/rbac/users/{id}/has-permission/{permissionName}` - Verificar permissão
  - `POST /api/admin/rbac/users/{id}/package` - Criar pacote
  - `GET /api/admin/rbac/packages/type/{type}` - Listar pacotes por tipo
  - `GET /api/admin/rbac/packages/expired` - Listar pacotes vencidos
  - `GET /api/admin/rbac/packages/valid` - Listar pacotes válidos

- **Entidades e Repositories:**
  - `Role` - Entidade para roles (ADMIN, CLINICO, etc.)
  - `Permission` - Entidade para permissões granulares
  - `UsuarioPacote` - Entidade para metadados de pacotes
  - `RoleRepository` - Repository com queries customizadas
  - `PermissionRepository` - Repository para permissões
  - `UsuarioPacoteRepository` - Repository para pacotes

- **Services:**
  - `RbacService` - Lógica completa de gerenciamento RBAC

- **Segurança:**
  - Autorização por roles (`@PreAuthorize`)
  - Endpoints ADMIN protegidos
  - Sistema de permissões granulares
  - Metadados de pacotes (limites, vencimento)

- **Banco de Dados:**
  - Migration V5: 5 tabelas RBAC (roles, permissions, role_permissions, usuario_roles, usuario_pacotes)
  - Dados iniciais: 2 roles (ADMIN, CLINICO) + 12 permissões
  - Índices otimizados para performance

- **Testes:**
  - Scripts organizados em `scripts/testes/rbac/`
  - Testes manuais completos
  - Documentação de testes

#### 🔧 Modificado
- `SecurityConfig.java` - Adicionada autorização RBAC
- `Usuario.java` - Relacionamentos com roles e pacotes
- `UsuarioRepository.java` - Métodos para RBAC
- `GlobalExceptionHandler.java` - Handlers para exceções RBAC

#### 📚 Documentação
- README.md atualizado para Fase 3
- Guia técnico completo atualizado
- Scripts de teste organizados
- Documentação RBAC completa

---

## [2.0.0] - 2025-10-14

### 🎉 Fase 2: Recuperação de Senha por Email - COMPLETA

#### ✨ Adicionado
- **4 novos endpoints REST:**
  - `POST /api/auth/password-reset/request` - Solicitar reset de senha
  - `GET /api/auth/password-reset/validate-token/{token}` - Validar token
  - `POST /api/auth/password-reset/confirm` - Confirmar nova senha
  - `GET /api/auth/password-reset/health` - Health check do serviço

- **Entidades e Repositories:**
  - `PasswordResetToken` - Entidade para tokens de reset
  - `PasswordResetAudit` - Entidade para auditoria LGPD
  - `PasswordResetTokenRepository` - Repository com queries customizadas
  - `PasswordResetAuditRepository` - Repository para auditoria

- **Services:**
  - `EmailService` - Envio de emails multipart com templates
  - `PasswordResetService` - Lógica completa de recuperação de senha

- **Segurança:**
  - Rate limiting (3 tentativas/hora por email/IP)
  - Anti-enumeração (resposta padronizada)
  - Tokens SHA-256 com uso único
  - Expiração de tokens (30 minutos)
  - Auditoria completa (LGPD compliance)
  - Delay anti-timing para emails inexistentes

- **Emails:**
  - Templates Thymeleaf (HTML + texto simples)
  - Internacionalização (pt-BR, en-US)
  - Email de reset de senha
  - Email de confirmação de alteração
  - MailHog configurado para desenvolvimento

- **Banco de Dados:**
  - Migration V2: Campo `email` na tabela `usuarios`
  - Migration V3: Tabela `password_reset_tokens`
  - Migration V4: Tabela `password_reset_audit`
  - 9 índices estratégicos para performance

- **Configurações:**
  - `I18nConfig` - Suporte a múltiplos idiomas
  - `@EnableScheduling` - Job de limpeza de tokens
  - Properties para MailHog (dev) e SMTP real (prod)

- **Utilities:**
  - `TokenUtils` - Geração e hash SHA-256 de tokens

- **DTOs:**
  - `PasswordResetRequestDto` - Request para solicitar reset
  - `PasswordResetConfirmDto` - Request para confirmar reset
  - `ApiResponse<T>` - Wrapper genérico para respostas (novos endpoints)

- **Exceptions:**
  - `TokenExpiredException` - Token expirado
  - `TokenInvalidException` - Token inválido ou já usado
  - `RateLimitExceededException` - Limite de tentativas excedido

- **Testes:**
  - 6 scripts PowerShell para testes E2E automatizados
  - 10 cenários de teste documentados
  - 100% de cobertura dos fluxos principais

- **Documentação:**
  - `GUIA_SETUP_DESENVOLVIMENTO.md` - Setup completo
  - `GUIA_TESTES.md` - Guia de testes
  - `TAREFA-2-REFERENCIA.md` - Decisões técnicas
  - `CHANGELOG.md` - Este arquivo
  - 8+ documentos técnicos detalhados

#### 🔧 Modificado
- `Usuario` - Adicionado campo `email` (unique, nullable)
- `RegisterRequest` - Adicionado campo `email` (validação @Email)
- `UserResponse` - Adicionado campo `email`
- `AuthenticationService` - Validação e salvamento de email
- `SecurityConfig` - Permitir acesso público aos endpoints de reset
- `GlobalExceptionHandler` - Handlers para novas exceptions
- `README.md` - Atualizado para versão 3.0 com novos endpoints
- `pom.xml` - Adicionadas dependências (mail, thymeleaf, commons-codec)

#### 🐛 Corrigido
- **Bug H2 Partial Index:** Removido `WHERE email IS NOT NULL` (H2 não suporta)
- **Bug TIMESTAMP:** Trocado `TIMESTAMP WITHOUT TIME ZONE` por `TIMESTAMP`
- **10 Problemas Críticos** identificados na revisão:
  1. ✅ Token hash comparison (BCrypt → SHA-256)
  2. ✅ Port mismatch (8081 → 8082)
  3. ✅ API response format (backward compatibility)
  4. ✅ RegisterRequest missing email field
  5. ✅ UserResponse missing email field
  6. ✅ SecurityConfig not updated
  7. ✅ Missing Thymeleaf dependency
  8. ✅ Missing MessageSource config
  9. ✅ Missing @EnableScheduling
  10. ✅ validateToken() same hash issue

#### 📊 Estatísticas
- **Commits:** 17
- **Arquivos Modificados:** 62
- **Linhas Adicionadas:** +12.411
- **Classes Java:** 30 (+16)
- **Endpoints:** 9 (+4)
- **Migrations:** 4 (+3)
- **Documentação:** ~7.500 linhas

---

## [2.1.0] - 2025-10-12

### 🔧 Fase 1: Correção de Persistência de Sessão

#### 🐛 Corrigido
- **Endpoint `/me` corrigido:** Sessão agora persiste corretamente
- **SecurityContext:** Implementado `HttpSessionSecurityContextRepository`
- **Tests:** Todos os 16 testes passando (100%)

#### 📚 Modificado
- `SecurityConfig` - Configurado `securityContextRepository`
- `GUIA_TÉCNICO_COMPLETO.md` - Atualizado status
- Collection Postman v2.0 - Testes ajustados

#### 📊 Estatísticas
- **Endpoints:** 5/5 operacionais (100%)
- **Testes:** 16/16 passando (100%)

---

## [2.0.0] - 2025-10-12

### 📚 Consolidação de Documentação

#### ✨ Adicionado
- `GUIA_TÉCNICO_COMPLETO.md` - Guia técnico consolidado (650 linhas)
- Documentação unificada e organizada

#### 🗑️ Removido
- Documentos redundantes (consolidados no guia técnico)
- 11 → 8 arquivos de documentação (-27%)

#### 📊 Estatísticas
- **Documentação:** 8 arquivos essenciais
- **Redundância:** Reduzida de ~40% para ~5%

---

## [1.0.0] - 2025-10-11

### 🎉 Fase 1: Sistema de Autenticação Básica - COMPLETA

#### ✨ Inicial Release
- **5 endpoints REST:**
  - `GET /api/auth/health` - Health check
  - `POST /api/auth/register` - Registro de usuários
  - `POST /api/auth/login` - Autenticação
  - `GET /api/auth/me` - Obter usuário atual
  - `POST /api/auth/logout` - Encerrar sessão

- **Segurança:**
  - Spring Security 6.2
  - BCrypt para senhas (força 12)
  - Sessões HTTP com cookies
  - CSRF habilitado
  - Validações completas

- **Banco de Dados:**
  - H2 (desenvolvimento)
  - PostgreSQL (produção)
  - Flyway migrations
  - Migration V1: Tabela `usuarios`

- **Entidades:**
  - `Usuario` - Entidade principal
  - `CustomUserDetailsService` - Integração com Spring Security

- **Testes:**
  - 16 testes automatizados na Collection Postman
  - Script PowerShell para testes
  - 100% de cobertura dos endpoints

- **Documentação:**
  - README.md completo
  - 8 documentos técnicos
  - Collection Postman v1.0

#### 📊 Estatísticas Iniciais
- **Classes Java:** 14
- **Linhas de Código:** ~2.500
- **Endpoints:** 5
- **Migrations:** 1
- **Testes:** 16

---

## 📋 Tipos de Mudanças

- `✨ Adicionado` - Novas funcionalidades
- `🔧 Modificado` - Mudanças em funcionalidades existentes
- `🗑️ Removido` - Funcionalidades removidas
- `🐛 Corrigido` - Correções de bugs
- `🔒 Segurança` - Correções de vulnerabilidades
- `📚 Documentação` - Apenas mudanças na documentação
- `🎯 Performance` - Melhorias de performance

---

## 🔗 Links Úteis

- **Repositório:** https://github.com/jocamposdot/neuroefficiency-backend
- **Documentação:** `DOCS/GUIA_TÉCNICO_COMPLETO.md`
- **Testes:** `DOCS/GUIA_TESTES.md`
- **Setup:** `DOCS/GUIA_SETUP_DESENVOLVIMENTO.md`

---

## 🚀 Próximas Versões

### [4.0.0] - Planejado
**Fase 3: RBAC - Controle de Acesso Baseado em Roles**
- Entidade `Role` (ADMIN, CLINICO, PACIENTE, SECRETARIA)
- Entidade `Permission`
- Autorização com `@PreAuthorize`
- Endpoints de gerenciamento de roles

### [5.0.0] - Planejado
**Fase 4: Rate Limiting Global e Hardening**
- Rate limiting global
- CSRF protection aprimorado
- HTTPS obrigatório
- Session timeout configurável

### [6.0.0] - Planejado
**Fase 5: Verificação de Email**
- Envio de email de verificação no registro
- Token de verificação
- Status de email verificado

---

**Mantido por:** Neuroefficiency Development Team  
**Última Atualização:** 14 de Outubro de 2025

