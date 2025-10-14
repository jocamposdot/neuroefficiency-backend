# 📋 Tarefa 2: Recuperação de Senha por E-mail
## Especificação Técnica Completa

**Data:** 14 de Outubro de 2025  
**Versão:** 1.0  
**Status:** 📝 Planejamento Completo  
**Branch:** `feature/segundo-passo-autenticacao`

---

## 📑 Índice

1. [Visão Geral](#1-visão-geral)
2. [Análise do Estado Atual](#2-análise-do-estado-atual)
3. [Decisões Arquiteturais](#3-decisões-arquiteturais)
4. [Integração Frontend-Backend](#4-integração-frontend-backend)
5. [Estrutura de Dados](#5-estrutura-de-dados)
6. [Especificação de Serviços](#6-especificação-de-serviços)
7. [Endpoints REST](#7-endpoints-rest)
8. [Segurança e Proteções](#8-segurança-e-proteções)
9. [Internacionalização](#9-internacionalização)
10. [Plano de Implementação](#10-plano-de-implementação)
11. [Testes](#11-testes)
12. [Critérios de Aceitação](#12-critérios-de-aceitação)

---

## 1. Visão Geral

### 1.1 Objetivo

Implementar funcionalidade completa de recuperação de senha por e-mail, permitindo que usuários que esqueceram suas credenciais possam redefini-las de forma segura através de um token enviado por e-mail.

### 1.2 Escopo

**Incluído nesta tarefa:**
- ✅ Adição do campo `email` à entidade `Usuario`
- ✅ Sistema de tokens de reset de senha com expiração
- ✅ Envio de e-mails multipart (texto + HTML)
- ✅ Suporte a múltiplos idiomas (pt-BR, en-US)
- ✅ Proteções anti-abuso (rate limiting, anti-enumeração)
- ✅ Auditoria completa de tentativas
- ✅ E-mail de confirmação após reset bem-sucedido
- ✅ Integração com frontend React existente

**Não incluído (futuras fases):**
- ❌ Logout global de todas as sessões (requer Spring Session + Redis)
- ❌ CAPTCHA (pode ser adicionado depois)
- ❌ Autenticação de dois fatores (2FA)

### 1.3 Paradigmas do Projeto

**Seguindo rigorosamente:**
- 🎯 **Gradualidade**: Implementação incremental, testada a cada etapa
- 📈 **Escalabilidade**: Código preparado para crescer (SMTP agnóstico)
- 🔧 **Extensibilidade**: Fácil adicionar novos idiomas, templates, etc.
- 🛡️ **Conservadorismo**: Mudanças mínimas ao código existente
- 🎨 **Minimamente Invasivo**: Não quebramos o que já funciona (Fase 1 intacta)

---

## 2. Análise do Estado Atual

### 2.1 Backend (neuro-core)

**Stack Tecnológica:**
```
Spring Boot:     3.5.6
Java:            21
Spring Security: 6.2.x
Spring Data JPA: 3.2.x
H2 Database:     2.3.232 (dev)
PostgreSQL:      latest (prod)
Flyway:          latest
BCrypt:          força 12
```

**Estrutura Atual:**
```
14 classes Java
16 testes passando (100%)
5 endpoints funcionais:
  - POST /api/auth/register
  - POST /api/auth/login
  - GET  /api/auth/me
  - POST /api/auth/logout
  - GET  /api/auth/health
```

**Entidade Usuario Atual:**
```java
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {
    private Long id;
    private String username;      // ⚠️ SEM email ainda
    private String passwordHash;
    private Boolean enabled;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 2.2 Frontend (neuroefficiency-front)

**Stack Tecnológica:**
```
React:       19.1.1
TypeScript:  5.9.3
Vite:        7.1.7
Tailwind:    3.4.18
State:       Context API
Tests:       Vitest (12 passando)
```

**Configuração:**
```typescript
Frontend URL:  http://localhost:5173
Backend URL:   http://localhost:8082/api
Auth:          Cookie JSESSIONID (sessões HTTP)
Timeout:       30 segundos
```

**Interface User do Frontend:**
```typescript
interface User {
  id: number;
  username: string;
  createdAt?: string;
  // ⚠️ Tem campo 'email' em types/index.ts mas não usado ainda
}
```

**Formato de Response Esperado:**
```typescript
interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  status: number;
}
```

### 2.3 Gaps Identificados

| Gap | Backend | Frontend | Ação Necessária |
|-----|---------|----------|-----------------|
| **Campo Email** | ❌ Não tem | ✅ Interface pronta | Adicionar no backend |
| **Formato Response** | ❌ Formato diferente | ✅ Espera padrão | Criar wrapper `ApiResponse<T>` |
| **Endpoint Reset** | ❌ Não existe | ❌ Não existe | Implementar ambos |
| **i18n** | ❌ Não tem | ❌ Não tem | Preparar backend, frontend depois |

---

## 3. Decisões Arquiteturais

### 3.1 Serviço de E-mail

**Decisão:** MailHog (dev) + SMTP agnóstico (prod)

**Justificativa:**
- ✅ Zero lock-in com provedores
- ✅ MailHog leve e com UI web (http://localhost:8025)
- ✅ Migração trivial para qualquer SMTP (AWS SES, SendGrid, etc.)
- ✅ Configuração via properties (12-factor app)

**Implementação:**
```properties
# Dev
spring.mail.host=localhost
spring.mail.port=1025

# Prod (variáveis de ambiente)
spring.mail.host=${SMTP_HOST}
spring.mail.port=${SMTP_PORT}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
```

### 3.2 Campo Email vs Username

**Decisão:** Adicionar `email` SEM remover `username` (Opção A)

**Justificativa:**
- ✅ **Minimamente Invasivo**: Fase 1 continua funcionando 100%
- ✅ **Gradual**: Podemos depreciar username depois se necessário
- ✅ **Flexível**: Usuário pode logar com username OU email
- ✅ **Sem refatoração**: 16 testes, Postman Collection, endpoints intactos

**Alternativa Rejeitada:**
- ❌ Substituir username por email = INVASIVO demais para Fase 2

### 3.3 Segurança e Proteções

**Decisão:** Proteção Moderada (balanceada)

**Implementado:**
- ✅ **Anti-enumeração**: Resposta sempre igual (email existe ou não)
- ✅ **Anti timing attack**: Delay artificial (100-300ms) para emails inexistentes
- ✅ **Rate limiting**: 3 tentativas por hora por IP + por email
- ✅ **Token hasheado**: NUNCA armazenar token em plain text
- ✅ **Expiração curta**: 30 minutos
- ✅ **Uso único**: Token invalidado após uso ou expiração
- ✅ **Auditoria**: Logs de TODAS as tentativas (sucesso e falha)

**Não Implementado Agora (futuro):**
- ⏳ CAPTCHA (pode adicionar depois sem mudanças grandes)
- ⏳ Device fingerprinting avançado

### 3.4 Tokens de Reset

**Decisão Aprovada:**
- ✅ **Expiração**: 30 minutos (balanceado)
- ✅ **Quantidade**: 1 token ativo por usuário (simples e seguro)
- ✅ **Invalidação**: Sim, sempre após uso bem-sucedido
- ✅ **Logout**: Apenas sessão atual (logout global requer Redis - Fase 6)

**Justificativa Logout Parcial:**
```
Logout global requer:
- Spring Session com Redis
- Infraestrutura de gerenciamento de sessões
- Não devemos bloquear Fase 2 por isso

Solução: 
- Fase 2: Logout apenas da sessão atual
- Fase 6: Implementar gestão completa de sessões + logout global
```

### 3.5 Templates de E-mail

**Decisão:** Multipart (texto + HTML simples)

**Justificativa:**
- ✅ **Compatibilidade**: Texto garante 100% de entrega
- ✅ **UX**: HTML melhora aparência
- ✅ **Extensível**: Preparado para branding futuro
- ✅ **Gradual**: Começa simples, evolui depois

**Conteúdo do E-mail:**
```
1. Link direto: https://app.neuroefficiency.com/reset-password?token=...
2. Token no corpo (fallback): abc123...
3. Informações de suporte no rodapé
4. Expiração clara: "Este link expira em 30 minutos"
```

### 3.6 Internacionalização

**Decisão:** Backend prepara múltiplos idiomas, frontend pode usar depois

**Idiomas Suportados Inicialmente:**
- 🇧🇷 Português (Brasil) - padrão
- 🇺🇸 English (US)
- 🇪🇸 Español (opcional, fácil adicionar)

**Detecção de Idioma:**
```java
// Header Accept-Language
Locale locale = LocaleContextHolder.getLocale();

// Ou explícito no request
public void requestReset(@RequestParam(defaultValue = "pt-BR") String locale)
```

---

## 4. Integração Frontend-Backend

### 4.1 Contrato de API

**Backend DEVE fornecer:**

```typescript
// Request: Solicitar Reset
POST /api/auth/password-reset/request
{
  "email": "user@example.com"
}

// Response (SEMPRE igual, exista ou não o email)
{
  "success": true,
  "data": null,
  "message": "Se o email existir, você receberá instruções"
}
```

```typescript
// Request: Confirmar Reset
POST /api/auth/password-reset/confirm
{
  "token": "abc123...",
  "newPassword": "NewPass@123",
  "confirmPassword": "NewPass@123"
}

// Response Sucesso
{
  "success": true,
  "data": null,
  "message": "Senha redefinida com sucesso"
}

// Response Erro
{
  "success": false,
  "data": null,
  "message": "Token inválido ou expirado"
}
```

```typescript
// Request: Validar Token (opcional, para UX)
GET /api/auth/password-reset/validate-token/{token}

// Response
{
  "success": true,
  "data": {
    "valid": true,
    "expired": false
  },
  "message": null
}
```

### 4.2 Adaptação de Responses Existentes

**Problema Atual:**
```java
// Backend retorna (formato antigo)
{
  "message": "Login realizado com sucesso",
  "user": { ... }
}

// Frontend espera (formato novo)
{
  "success": true,
  "data": { ... },
  "message": "Login realizado com sucesso"
}
```

**Solução: Criar DTO Wrapper**
```java
@Data
@Builder
public class ApiResponse<T> {
    private Boolean success;
    private T data;
    private String message;
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .build();
    }
}
```

**Aplicar em TODOS os endpoints:**
```java
// Antes
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(...) {
    return ResponseEntity.ok(authResponse);
}

// Depois
@PostMapping("/login")
public ResponseEntity<ApiResponse<UserResponse>> login(...) {
    UserResponse user = authenticationService.login(...);
    return ResponseEntity.ok(
        ApiResponse.success(user, "Login realizado com sucesso")
    );
}
```

### 4.3 Configuração de URL do Frontend

```properties
# application.properties
app.frontend.url=${FRONTEND_URL:http://localhost:5173}

# Usado para gerar links nos emails
String resetLink = frontendUrl + "/reset-password?token=" + token;
```

---

## 5. Estrutura de Dados

### 5.1 Migration V2: Adicionar Email

**Arquivo:** `src/main/resources/db/migration/V2__add_email_to_usuarios.sql`

```sql
-- Adicionar coluna email
ALTER TABLE usuarios ADD COLUMN email VARCHAR(255);

-- Para registros existentes, gerar emails temporários
-- (permite que fase 1 continue funcionando sem quebrar)
UPDATE usuarios 
SET email = CONCAT(username, '@temp.neuroefficiency.local') 
WHERE email IS NULL;

-- Tornar obrigatório e único
ALTER TABLE usuarios ALTER COLUMN email SET NOT NULL;
ALTER TABLE usuarios ADD CONSTRAINT uk_usuarios_email UNIQUE (email);

-- Criar índice para performance
CREATE INDEX idx_usuarios_email ON usuarios(email);

-- Comentário para documentar
COMMENT ON COLUMN usuarios.email IS 'Email do usuário para autenticação e comunicação';
```

### 5.2 Migration V3: Tabela de Tokens

**Arquivo:** `src/main/resources/db/migration/V3__create_password_reset_tokens.sql`

```sql
CREATE TABLE password_reset_tokens (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    used_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT fk_password_reset_tokens_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_usuario_id ON password_reset_tokens(usuario_id);
CREATE INDEX idx_password_reset_tokens_token_hash ON password_reset_tokens(token_hash);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);

COMMENT ON TABLE password_reset_tokens IS 'Tokens de recuperação de senha com expiração';
COMMENT ON COLUMN password_reset_tokens.token_hash IS 'Hash BCrypt do token (nunca armazenar plain text)';
COMMENT ON COLUMN password_reset_tokens.used_at IS 'Quando o token foi usado (null = ainda não usado)';
```

### 5.3 Migration V4: Tabela de Auditoria

**Arquivo:** `src/main/resources/db/migration/V4__create_password_reset_audit.sql`

```sql
CREATE TABLE password_reset_audit (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    event_type VARCHAR(50) NOT NULL,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_password_reset_audit PRIMARY KEY (id)
);

CREATE INDEX idx_password_reset_audit_email ON password_reset_audit(email);
CREATE INDEX idx_password_reset_audit_ip ON password_reset_audit(ip_address);
CREATE INDEX idx_password_reset_audit_timestamp ON password_reset_audit(timestamp);
CREATE INDEX idx_password_reset_audit_event_type ON password_reset_audit(event_type);

COMMENT ON TABLE password_reset_audit IS 'Auditoria de tentativas de recuperação de senha (compliance LGPD)';
```

### 5.4 Entidade Usuario Atualizada

```java
@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // ✨ NOVO: Campo email
    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Password é obrigatório")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;

    @Builder.Default
    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = true;

    @Builder.Default
    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // UserDetails methods...
}
```

### 5.5 Entidade PasswordResetToken

```java
@Entity
@Table(name = "password_reset_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isValid() {
        return !isExpired() && !isUsed();
    }
}
```

### 5.6 Entidade PasswordResetAudit

```java
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

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AuditEventType eventType;

    @Column(nullable = false)
    private Boolean success;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}

public enum AuditEventType {
    REQUEST,          // Solicitação de reset
    SUCCESS,          // Reset bem-sucedido
    FAILURE,          // Falha no reset
    EXPIRED_TOKEN,    // Tentativa com token expirado
    INVALID_TOKEN,    // Token inválido
    RATE_LIMIT        // Bloqueado por rate limit
}
```

### 5.7 Repositories

```java
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);  // ✨ NOVO
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);          // ✨ NOVO
}

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :usedAt WHERE t.usuario.id = :usuarioId AND t.usedAt IS NULL")
    void invalidateAllByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("usedAt") LocalDateTime usedAt);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
    
    long countByUsuarioIdAndCreatedAtAfter(Long usuarioId, LocalDateTime after);
}

@Repository
public interface PasswordResetAuditRepository extends JpaRepository<PasswordResetAudit, Long> {
    long countByEmailAndTimestampAfter(String email, LocalDateTime after);
    long countByIpAddressAndTimestampAfter(String ipAddress, LocalDateTime after);
    
    List<PasswordResetAudit> findByEmailOrderByTimestampDesc(String email, Pageable pageable);
}
```

---

## 6. Especificação de Serviços

### 6.1 EmailService

**Responsabilidade:** Enviar e-mails multipart com templates Thymeleaf e i18n.

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.email.from:noreply@neuroefficiency.com}")
    private String fromEmail;

    /**
     * Envia email de reset de senha
     */
    public void sendPasswordResetEmail(Usuario usuario, String token, Locale locale) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            // Context para Thymeleaf
            Context context = new Context(locale);
            context.setVariable("username", usuario.getUsername());
            context.setVariable("email", usuario.getEmail());
            context.setVariable("resetLink", resetLink);
            context.setVariable("token", token);
            context.setVariable("expirationMinutes", 30);

            // HTML via Thymeleaf
            String htmlContent = templateEngine.process("email/password-reset", context);

            // Texto simples (fallback)
            String textContent = messageSource.getMessage(
                "email.reset.text",
                new Object[]{resetLink, token},
                locale
            );

            helper.setFrom(fromEmail);
            helper.setTo(usuario.getEmail());
            helper.setSubject(messageSource.getMessage("email.reset.subject", null, locale));
            helper.setText(textContent, htmlContent);

            mailSender.send(message);

            log.info("Email de reset enviado para: {}", sanitizeEmail(usuario.getEmail()));

        } catch (MessagingException e) {
            log.error("Erro ao enviar email de reset para: {}", sanitizeEmail(usuario.getEmail()), e);
            throw new EmailSendingException("Falha ao enviar email", e);
        }
    }

    /**
     * Envia email de confirmação após reset bem-sucedido
     */
    public void sendPasswordChangedConfirmation(Usuario usuario, Locale locale) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context(locale);
            context.setVariable("username", usuario.getUsername());
            context.setVariable("changeDate", LocalDateTime.now());

            String htmlContent = templateEngine.process("email/password-changed", context);
            String textContent = messageSource.getMessage(
                "email.changed.text",
                new Object[]{usuario.getUsername(), LocalDateTime.now()},
                locale
            );

            helper.setFrom(fromEmail);
            helper.setTo(usuario.getEmail());
            helper.setSubject(messageSource.getMessage("email.changed.subject", null, locale));
            helper.setText(textContent, htmlContent);

            mailSender.send(message);

            log.info("Email de confirmação enviado para: {}", sanitizeEmail(usuario.getEmail()));

        } catch (MessagingException e) {
            log.error("Erro ao enviar email de confirmação para: {}", sanitizeEmail(usuario.getEmail()), e);
            // Não lançar exceção aqui - falha no envio de confirmação não deve bloquear o reset
        }
    }

    private String sanitizeEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        return parts[0].charAt(0) + "***@" + parts[1];
    }
}
```

### 6.2 PasswordResetService

**Responsabilidade:** Lógica de negócio de reset de senha com proteções.

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetAuditRepository auditRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRATION_MINUTES = 30;
    private static final int MAX_REQUESTS_PER_HOUR = 3;
    private static final int ARTIFICIAL_DELAY_MIN_MS = 100;
    private static final int ARTIFICIAL_DELAY_MAX_MS = 300;

    /**
     * Solicita reset de senha
     */
    @Transactional
    public void requestPasswordReset(String email, String ipAddress, String userAgent, Locale locale) {
        
        // 1. Resposta padronizada (anti-enumeração)
        String standardResponse = messageSource.getMessage("password.reset.request.response", null, locale);

        try {
            // 2. Buscar usuário
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

            if (usuarioOpt.isEmpty()) {
                // Delay artificial (anti timing attack)
                simulateProcessing();
                auditFailedRequest(email, ipAddress, userAgent, "Email não encontrado");
                return; // Mesma resposta
            }

            Usuario usuario = usuarioOpt.get();

            // 3. Rate limiting
            if (hasExceededRateLimit(email, ipAddress)) {
                auditRateLimitExceeded(email, ipAddress, userAgent);
                throw new RateLimitExceededException(
                    messageSource.getMessage("password.reset.rate.limit", null, locale)
                );
            }

            // 4. Invalidar tokens anteriores
            tokenRepository.invalidateAllByUsuarioId(usuario.getId(), LocalDateTime.now());

            // 5. Gerar novo token
            String token = generateSecureToken();
            String tokenHash = passwordEncoder.encode(token);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .usuario(usuario)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES))
                .build();

            tokenRepository.save(resetToken);

            // 6. Enviar email
            emailService.sendPasswordResetEmail(usuario, token, locale);

            // 7. Auditar sucesso
            auditSuccessfulRequest(email, ipAddress, userAgent);

            log.info("Reset solicitado para: {}", sanitizeEmail(email));

        } catch (RateLimitExceededException e) {
            throw e; // Propagar para controller
        } catch (Exception e) {
            log.error("Erro ao processar request de reset para: {}", sanitizeEmail(email), e);
            auditFailedRequest(email, ipAddress, userAgent, e.getMessage());
            // Não propagar - retornar resposta padrão
        }
    }

    /**
     * Confirma reset de senha com token
     */
    @Transactional
    public void confirmPasswordReset(String token, String newPassword, String ipAddress, String userAgent, Locale locale) {

        // 1. Buscar token
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(
            passwordEncoder.encode(token)
        ).orElseThrow(() -> new InvalidTokenException(
            messageSource.getMessage("password.reset.token.invalid", null, locale)
        ));

        // 2. Validar token
        if (resetToken.isExpired()) {
            auditExpiredToken(resetToken.getUsuario().getEmail(), ipAddress, userAgent);
            throw new TokenExpiredException(
                messageSource.getMessage("password.reset.token.expired", null, locale)
            );
        }

        if (resetToken.isUsed()) {
            auditInvalidToken(resetToken.getUsuario().getEmail(), ipAddress, userAgent, "Token já usado");
            throw new InvalidTokenException(
                messageSource.getMessage("password.reset.token.used", null, locale)
            );
        }

        Usuario usuario = resetToken.getUsuario();

        // 3. Atualizar senha
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        // 4. Marcar token como usado
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        // 5. Enviar email de confirmação
        emailService.sendPasswordChangedConfirmation(usuario, locale);

        // 6. Auditar sucesso
        auditSuccessfulReset(usuario.getEmail(), ipAddress, userAgent);

        // 7. Logout da sessão atual (se estiver autenticado)
        SecurityContextHolder.clearContext();

        log.info("Senha redefinida com sucesso para: {}", sanitizeEmail(usuario.getEmail()));
    }

    /**
     * Valida se token é válido
     */
    @Transactional(readOnly = true)
    public TokenValidationResponse validateToken(String token) {
        try {
            PasswordResetToken resetToken = tokenRepository.findByTokenHash(
                passwordEncoder.encode(token)
            ).orElse(null);

            if (resetToken == null) {
                return TokenValidationResponse.invalid();
            }

            return TokenValidationResponse.builder()
                .valid(!resetToken.isExpired() && !resetToken.isUsed())
                .expired(resetToken.isExpired())
                .used(resetToken.isUsed())
                .build();

        } catch (Exception e) {
            log.error("Erro ao validar token", e);
            return TokenValidationResponse.invalid();
        }
    }

    // ========== Métodos Auxiliares ==========

    private boolean hasExceededRateLimit(String email, String ipAddress) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        long emailCount = auditRepository.countByEmailAndTimestampAfter(email, oneHourAgo);
        long ipCount = auditRepository.countByIpAddressAndTimestampAfter(ipAddress, oneHourAgo);

        return emailCount >= MAX_REQUESTS_PER_HOUR || ipCount >= MAX_REQUESTS_PER_HOUR;
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }

    private void simulateProcessing() {
        try {
            long delay = ThreadLocalRandom.current().nextLong(
                ARTIFICIAL_DELAY_MIN_MS,
                ARTIFICIAL_DELAY_MAX_MS
            );
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Métodos de auditoria...
    private void auditSuccessfulRequest(String email, String ipAddress, String userAgent) {
        PasswordResetAudit audit = PasswordResetAudit.builder()
            .email(email)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .eventType(AuditEventType.REQUEST)
            .success(true)
            .build();
        auditRepository.save(audit);
    }

    // ... outros métodos de auditoria
}
```

### 6.3 Job de Limpeza de Tokens Expirados

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupJob {

    private final PasswordResetTokenRepository tokenRepository;

    /**
     * Executa diariamente às 3h da manhã
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Iniciando limpeza de tokens expirados");

        try {
            tokenRepository.deleteExpired(LocalDateTime.now());
            log.info("Tokens expirados removidos com sucesso");
        } catch (Exception e) {
            log.error("Erro ao limpar tokens expirados", e);
        }
    }
}
```

---

## 7. Endpoints REST

### 7.1 PasswordResetController

```java
@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Solicita reset de senha
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> requestReset(
            @Valid @RequestBody PasswordResetRequestDto request,
            @RequestHeader(value = "Accept-Language", defaultValue = "pt-BR") String languageHeader,
            HttpServletRequest httpRequest) {

        Locale locale = Locale.forLanguageTag(languageHeader.split(",")[0]);
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        passwordResetService.requestPasswordReset(
            request.getEmail(),
            ipAddress,
            userAgent,
            locale
        );

        return ResponseEntity.ok(
            ApiResponse.success(null, "Se o email existir, você receberá instruções")
        );
    }

    /**
     * Confirma reset de senha com token
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmReset(
            @Valid @RequestBody PasswordResetConfirmDto request,
            @RequestHeader(value = "Accept-Language", defaultValue = "pt-BR") String languageHeader,
            HttpServletRequest httpRequest) {

        Locale locale = Locale.forLanguageTag(languageHeader.split(",")[0]);
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        passwordResetService.confirmPasswordReset(
            request.getToken(),
            request.getNewPassword(),
            ipAddress,
            userAgent,
            locale
        );

        return ResponseEntity.ok(
            ApiResponse.success(null, "Senha redefinida com sucesso")
        );
    }

    /**
     * Valida se token é válido (opcional, para UX)
     */
    @GetMapping("/validate-token/{token}")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(
            @PathVariable String token) {

        TokenValidationResponse validation = passwordResetService.validateToken(token);

        return ResponseEntity.ok(
            ApiResponse.success(validation, null)
        );
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

### 7.2 DTOs

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDto {

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    private String email;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetConfirmDto {

    @NotBlank(message = "Token é obrigatório")
    private String token;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "Senha deve conter maiúscula, minúscula, número e caractere especial"
    )
    private String newPassword;

    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmPassword;

    @AssertTrue(message = "As senhas não coincidem")
    public boolean isPasswordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}

@Data
@Builder
public class TokenValidationResponse {
    private Boolean valid;
    private Boolean expired;
    private Boolean used;

    public static TokenValidationResponse invalid() {
        return TokenValidationResponse.builder()
            .valid(false)
            .expired(false)
            .used(false)
            .build();
    }
}
```

---

## 8. Segurança e Proteções

### 8.1 Anti-Enumeração

**Objetivo:** Impedir que atacantes descubram quais emails existem no sistema.

**Implementação:**
```java
// SEMPRE retornar a mesma resposta
String standardResponse = "Se o email existir, você receberá instruções";

if (usuarioNotFound) {
    // Delay artificial
    Thread.sleep(random(100, 300));
    return standardResponse;
}

// Email encontrado
sendEmail(...);
return standardResponse; // MESMA resposta
```

### 8.2 Rate Limiting

**Objetivo:** Prevenir abuso e brute force.

**Regras:**
- Máximo 3 tentativas por hora por email
- Máximo 3 tentativas por hora por IP
- Bloqueio temporário de 1 hora

**Implementação:**
```java
LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

long emailCount = auditRepository.countByEmailAndTimestampAfter(email, oneHourAgo);
long ipCount = auditRepository.countByIpAddressAndTimestampAfter(ipAddress, oneHourAgo);

if (emailCount >= 3 || ipCount >= 3) {
    throw new RateLimitExceededException("Muitas tentativas. Tente novamente em 1 hora.");
}
```

### 8.3 Token Security

**Geração:**
```java
// 64 caracteres hexadecimais (256 bits de entropia)
String token = UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
// Exemplo: "a1b2c3d4...64 chars"
```

**Armazenamento:**
```java
// NUNCA armazenar token em plain text
String tokenHash = passwordEncoder.encode(token); // BCrypt
resetToken.setTokenHash(tokenHash);
```

**Validação:**
```java
// Comparar hash
PasswordResetToken resetToken = tokenRepository.findByTokenHash(
    passwordEncoder.encode(receivedToken)
).orElseThrow();
```

### 8.4 Expiração Rigorosa

```java
// 30 minutos
LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

// Validação
if (LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
    throw new TokenExpiredException("Token expirado");
}
```

### 8.5 Uso Único

```java
// Marcar como usado
resetToken.setUsedAt(LocalDateTime.now());

// Prevenir reuso
if (resetToken.getUsedAt() != null) {
    throw new InvalidTokenException("Token já utilizado");
}
```

---

## 9. Internacionalização

### 9.1 Arquivos de Mensagens

**messages_pt_BR.properties:**
```properties
# Reset de Senha
password.reset.request.response=Se o email existir, você receberá instruções para redefinir sua senha
password.reset.rate.limit=Muitas tentativas. Tente novamente em 1 hora
password.reset.token.invalid=Token inválido ou não encontrado
password.reset.token.expired=Token expirado. Solicite um novo reset de senha
password.reset.token.used=Token já foi utilizado

# Email - Assunto
email.reset.subject=Redefinir sua senha - Neuroefficiency
email.changed.subject=Sua senha foi alterada - Neuroefficiency

# Email - Corpo Texto
email.reset.text=Olá! Você solicitou redefinição de senha.\n\nClique no link: {0}\n\nOu use o token: {1}\n\nEste link expira em 30 minutos.\n\nSe você não solicitou, ignore este email.
email.changed.text=Olá {0},\n\nSua senha foi alterada em {1}.\n\nSe você não fez esta alteração, entre em contato imediatamente.

# Email - HTML
email.reset.title=Solicitação de Redefinição de Senha
email.reset.greeting=Olá, {0}!
email.reset.intro=Você solicitou redefinição de senha para sua conta Neuroefficiency.
email.reset.button=Redefinir Senha
email.reset.token.label=Token (alternativa):
email.reset.expiration=Este link expira em {0} minutos
email.reset.ignore=Se você não solicitou esta redefinição, ignore este email.
email.reset.support=Precisa de ajuda? Visite nosso
email.reset.support.link=Centro de Suporte

email.changed.title=Senha Alterada com Sucesso
email.changed.greeting=Olá, {0}!
email.changed.message=Sua senha foi alterada com sucesso em {1}.
email.changed.warning=Se você não fez esta alteração, sua conta pode estar comprometida.
email.changed.action=Entre em contato com nosso suporte imediatamente.
```

**messages_en_US.properties:**
```properties
# Password Reset
password.reset.request.response=If the email exists, you will receive instructions to reset your password
password.reset.rate.limit=Too many attempts. Try again in 1 hour
password.reset.token.invalid=Invalid or not found token
password.reset.token.expired=Token expired. Request a new password reset
password.reset.token.used=Token has already been used

# Email - Subject
email.reset.subject=Reset your password - Neuroefficiency
email.changed.subject=Your password was changed - Neuroefficiency

# Email - Text Body
email.reset.text=Hello! You requested a password reset.\n\nClick the link: {0}\n\nOr use the token: {1}\n\nThis link expires in 30 minutes.\n\nIf you didn't request this, ignore this email.
email.changed.text=Hello {0},\n\nYour password was changed on {1}.\n\nIf you didn't make this change, contact us immediately.

# Email - HTML
email.reset.title=Password Reset Request
email.reset.greeting=Hello, {0}!
email.reset.intro=You requested a password reset for your Neuroefficiency account.
email.reset.button=Reset Password
email.reset.token.label=Token (alternative):
email.reset.expiration=This link expires in {0} minutes
email.reset.ignore=If you didn't request this reset, ignore this email.
email.reset.support=Need help? Visit our
email.reset.support.link=Support Center

email.changed.title=Password Successfully Changed
email.changed.greeting=Hello, {0}!
email.changed.message=Your password was successfully changed on {1}.
email.changed.warning=If you didn't make this change, your account may be compromised.
email.changed.action=Contact our support immediately.
```

### 9.2 Templates Thymeleaf

**password-reset.html:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="#{email.reset.title}">Password Reset</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
    <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
        <h1 style="color: white; margin: 0;" th:text="#{email.reset.title}">Password Reset Request</h1>
    </div>
    
    <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
        <p style="font-size: 16px;" th:text="#{email.reset.greeting(${username})}">Hello!</p>
        
        <p th:text="#{email.reset.intro}">You requested a password reset.</p>
        
        <div style="text-align: center; margin: 30px 0;">
            <a th:href="${resetLink}" 
               style="background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">
                [[#{email.reset.button}]]
            </a>
        </div>
        
        <div style="background: white; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #667eea;">
            <p style="margin: 0; font-size: 14px;"><strong th:text="#{email.reset.token.label}">Token:</strong></p>
            <code style="background: #f0f0f0; padding: 5px 10px; display: block; margin-top: 5px; word-break: break-all;">[[${token}]]</code>
        </div>
        
        <p style="font-size: 14px; color: #666;">
            <strong th:text="#{email.reset.expiration(${expirationMinutes})}">Expires in 30 minutes</strong>
        </p>
        
        <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
        
        <p style="font-size: 13px; color: #999;" th:text="#{email.reset.ignore}">
            If you didn't request this, ignore this email.
        </p>
        
        <p style="font-size: 13px; color: #999; text-align: center;">
            [[#{email.reset.support}]] 
            <a href="https://app.neuroefficiency.com/help" style="color: #667eea;" th:text="#{email.reset.support.link}">Support</a>
        </p>
    </div>
</body>
</html>
```

**password-changed.html:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="#{email.changed.title}">Password Changed</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
    <div style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
        <h1 style="color: white; margin: 0;" th:text="#{email.changed.title}">Password Changed</h1>
    </div>
    
    <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
        <p style="font-size: 16px;" th:text="#{email.changed.greeting(${username})}">Hello!</p>
        
        <p th:text="#{email.changed.message(${changeDate})}">Your password was changed.</p>
        
        <div style="background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 5px;">
            <p style="margin: 0; color: #856404;">
                <strong th:text="#{email.changed.warning}">Warning:</strong>
            </p>
            <p style="margin: 10px 0 0 0; color: #856404;" th:text="#{email.changed.action}">
                Contact support if you didn't make this change.
            </p>
        </div>
        
        <p style="font-size: 13px; color: #999; text-align: center;">
            <a href="https://app.neuroefficiency.com/help" style="color: #11998e;">Support Center</a>
        </p>
    </div>
</body>
</html>
```

---

## 10. Plano de Implementação

### 📅 Cronograma Detalhado

| Etapa | Descrição | Duração | Status |
|-------|-----------|---------|--------|
| **1** | Configuração de Infraestrutura | 1 dia | ⏳ Pendente |
| **2** | Estrutura de Dados (Migrations + Entidades) | 1 dia | ⏳ Pendente |
| **3** | DTOs e Responses Padronizadas | 0.5 dia | ⏳ Pendente |
| **4** | EmailService (Templates + i18n) | 1 dia | ⏳ Pendente |
| **5** | PasswordResetService | 1.5 dias | ⏳ Pendente |
| **6** | Controllers e Endpoints | 0.5 dia | ⏳ Pendente |
| **7** | Internacionalização | 0.5 dia | ⏳ Pendente |
| **8** | Testes Unitários | 1 dia | ⏳ Pendente |
| **9** | Testes de Integração | 1 dia | ⏳ Pendente |
| **10** | Documentação e Postman | 0.5 dia | ⏳ Pendente |
| **11** | Revisão e Ajustes Finais | 0.5 dia | ⏳ Pendente |
| | **TOTAL** | **9 dias** | |

### Etapa 1: Configuração de Infraestrutura (1 dia)

**Tarefas:**
1. ✅ Adicionar dependências ao `pom.xml`
2. ✅ Configurar properties de email (dev/test/prod)
3. ✅ Instalar e configurar MailHog
4. ✅ Testar envio de email básico

**Entregas:**
- `pom.xml` atualizado
- Properties configurados
- MailHog rodando
- Teste de envio funcionando

### Etapa 2: Estrutura de Dados (1 dia)

**Tarefas:**
1. ✅ Criar migration V2 (adicionar email)
2. ✅ Criar migration V3 (tabela tokens)
3. ✅ Criar migration V4 (tabela audit)
4. ✅ Atualizar entidade Usuario
5. ✅ Criar entidade PasswordResetToken
6. ✅ Criar entidade PasswordResetAudit
7. ✅ Criar repositories
8. ✅ Testar migrations

**Entregas:**
- 3 migrations funcionando
- 3 entidades criadas
- 3 repositories criados
- Banco atualizado

### Etapa 3: DTOs e Responses (0.5 dia)

**Tarefas:**
1. ✅ Criar ApiResponse wrapper
2. ✅ Criar PasswordResetRequestDto
3. ✅ Criar PasswordResetConfirmDto
4. ✅ Criar TokenValidationResponse
5. ✅ Atualizar endpoints existentes para usar ApiResponse

**Entregas:**
- DTOs criados
- Wrapper ApiResponse funcionando
- Endpoints atualizados

### Etapa 4: EmailService (1 dia)

**Tarefas:**
1. ✅ Criar EmailService
2. ✅ Criar templates Thymeleaf (reset + confirmação)
3. ✅ Configurar i18n (pt-BR + en-US)
4. ✅ Testar envio de emails
5. ✅ Testar templates em múltiplos idiomas

**Entregas:**
- EmailService funcionando
- 2 templates HTML criados
- i18n configurado
- Emails sendo enviados

### Etapa 5: PasswordResetService (1.5 dias)

**Tarefas:**
1. ✅ Implementar requestPasswordReset
2. ✅ Implementar confirmPasswordReset
3. ✅ Implementar validateToken
4. ✅ Adicionar rate limiting
5. ✅ Adicionar anti-enumeração
6. ✅ Adicionar auditoria
7. ✅ Criar job de limpeza
8. ✅ Testar fluxo completo

**Entregas:**
- PasswordResetService completo
- Proteções implementadas
- Auditoria funcionando
- Job de limpeza configurado

### Etapa 6: Controllers (0.5 dia)

**Tarefas:**
1. ✅ Criar PasswordResetController
2. ✅ Implementar endpoints
3. ✅ Adicionar tratamento de erros
4. ✅ Testar endpoints via Postman

**Entregas:**
- Controller criado
- 3 endpoints funcionando
- Error handling implementado

### Etapa 7: Internacionalização (0.5 dia)

**Tarefas:**
1. ✅ Criar messages_pt_BR.properties
2. ✅ Criar messages_en_US.properties
3. ✅ Configurar MessageSource
4. ✅ Testar troca de idiomas

**Entregas:**
- Arquivos de mensagens criados
- i18n funcionando
- Testes em múltiplos idiomas

### Etapa 8: Testes Unitários (1 dia)

**Tarefas:**
1. ✅ Testar EmailService
2. ✅ Testar PasswordResetService
3. ✅ Testar rate limiting
4. ✅ Testar token validation
5. ✅ Testar auditoria

**Entregas:**
- Mínimo 12 testes unitários
- Cobertura > 80%

### Etapa 9: Testes de Integração (1 dia)

**Tarefas:**
1. ✅ Testar fluxo completo de reset
2. ✅ Testar proteções anti-abuso
3. ✅ Testar expiração de tokens
4. ✅ Testar múltiplos idiomas
5. ✅ Testar endpoints REST

**Entregas:**
- Mínimo 8 testes de integração
- Fluxo end-to-end funcionando

### Etapa 10: Documentação (0.5 dia)

**Tarefas:**
1. ✅ Atualizar Postman Collection
2. ✅ Atualizar README.md
3. ✅ Criar guia de integração frontend
4. ✅ Documentar novos endpoints

**Entregas:**
- Postman Collection v2.0
- Documentação atualizada
- Guia de integração criado

### Etapa 11: Revisão Final (0.5 dia)

**Tarefas:**
1. ✅ Code review completo
2. ✅ Verificar linters
3. ✅ Testar em ambiente limpo
4. ✅ Validar critérios de aceitação
5. ✅ Merge para main

**Entregas:**
- Código revisado
- Todos os testes passando
- Branch merged

---

## 11. Testes

### 11.1 Testes Unitários (Mínimo 12)

**PasswordResetServiceTest:**
```java
@SpringBootTest
@Transactional
class PasswordResetServiceTest {

    @Test
    @DisplayName("Deve solicitar reset com sucesso para email existente")
    void shouldRequestResetForExistingEmail() {
        // Given
        Usuario usuario = criarUsuarioTeste("user@test.com");
        
        // When
        passwordResetService.requestPasswordReset(
            "user@test.com", "192.168.1.1", "Mozilla", Locale.forLanguageTag("pt-BR")
        );
        
        // Then
        verify(emailService).sendPasswordResetEmail(any(), any(), any());
        assertThat(tokenRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Não deve revelar se email não existe (anti-enumeração)")
    void shouldNotRevealNonExistentEmail() {
        // When
        passwordResetService.requestPasswordReset(
            "naoexiste@test.com", "192.168.1.1", "Mozilla", Locale.forLanguageTag("pt-BR")
        );
        
        // Then
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
        assertThat(tokenRepository.findAll()).isEmpty();
        // Mesma resposta seria retornada
    }

    @Test
    @DisplayName("Deve bloquear após exceder rate limit")
    void shouldBlockAfterRateLimit() {
        // Given
        Usuario usuario = criarUsuarioTeste("user@test.com");
        
        // When - 3 tentativas OK
        for (int i = 0; i < 3; i++) {
            passwordResetService.requestPasswordReset(
                "user@test.com", "192.168.1.1", "Mozilla", Locale.forLanguageTag("pt-BR")
            );
        }
        
        // Then - 4ª tentativa deve falhar
        assertThatThrownBy(() -> 
            passwordResetService.requestPasswordReset(
                "user@test.com", "192.168.1.1", "Mozilla", Locale.forLanguageTag("pt-BR")
            )
        ).isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    @DisplayName("Deve confirmar reset com token válido")
    void shouldConfirmResetWithValidToken() {
        // Given
        Usuario usuario = criarUsuarioTeste("user@test.com");
        String token = criarTokenValido(usuario);
        
        // When
        passwordResetService.confirmPasswordReset(
            token, "NewPass@123", "192.168.1.1", "Mozilla", Locale.forLanguageTag("pt-BR")
        );
        
        // Then
        Usuario updated = usuarioRepository.findById(usuario.getId()).get();
        assertThat(passwordEncoder.matches("NewPass@123", updated.getPasswordHash())).isTrue();
        verify(emailService).sendPasswordChangedConfirmation(any(), any());
    }

    @Test
    @DisplayName("Deve rejeitar token expirado")
    void shouldRejectExpiredToken() {
        // Given
        Usuario usuario = criarUsuarioTeste("user@test.com");
        String token = criarTokenExpirado(usuario);
        
        // When/Then
        assertThatThrownBy(() -> 
            passwordResetService.confirmPasswordReset(
                token, "NewPass@123", "192.168.1.1", "Mozilla", Locale.forLanguageTag("pt-BR")
            )
        ).isInstanceOf(TokenExpiredException.class);
    }

    @Test
    @DisplayName("Deve rejeitar token já usado")
    void shouldRejectUsedToken() {
        // Given
        Usuario usuario = criarUsuarioTeste("user@test.com");
        String token = criarTokenUsado(usuario);
        
        // When/Then
        assertThatThrownBy(() -> 
            passwordResetService.confirmPasswordReset(
                token, "NewPass@123", "192.168.1.1", "Mozilla", Locale.forLanguageTag("pt-BR")
            )
        ).isInstanceOf(InvalidTokenException.class);
    }

    // Mais testes...
}
```

**EmailServiceTest:**
```java
@SpringBootTest
class EmailServiceTest {

    @Test
    @DisplayName("Deve enviar email de reset em português")
    void shouldSendResetEmailInPortuguese() {
        // Given
        Usuario usuario = criarUsuarioTeste();
        String token = "abc123";
        Locale locale = Locale.forLanguageTag("pt-BR");
        
        // When
        emailService.sendPasswordResetEmail(usuario, token, locale);
        
        // Then
        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages[0].getSubject()).contains("Redefinir sua senha");
    }

    @Test
    @DisplayName("Deve enviar email de reset em inglês")
    void shouldSendResetEmailInEnglish() {
        // Given
        Usuario usuario = criarUsuarioTeste();
        String token = "abc123";
        Locale locale = Locale.US;
        
        // When
        emailService.sendPasswordResetEmail(usuario, token, locale);
        
        // Then
        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages[0].getSubject()).contains("Reset your password");
    }

    // Mais testes...
}
```

### 11.2 Testes de Integração (Mínimo 8)

**PasswordResetControllerIntegrationTest:**
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PasswordResetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar 200 ao solicitar reset para email existente")
    void shouldReturn200ForExistingEmail() throws Exception {
        // Given
        criarUsuarioTeste("user@test.com");
        PasswordResetRequestDto request = new PasswordResetRequestDto("user@test.com");

        // When/Then
        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("instruções")));
    }

    @Test
    @DisplayName("Deve retornar mesma resposta para email inexistente (anti-enumeração)")
    void shouldReturnSameResponseForNonExistentEmail() throws Exception {
        // Given
        PasswordResetRequestDto request = new PasswordResetRequestDto("naoexiste@test.com");

        // When/Then
        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("instruções")));
    }

    @Test
    @DisplayName("Deve retornar 400 para email inválido")
    void shouldReturn400ForInvalidEmail() throws Exception {
        // Given
        PasswordResetRequestDto request = new PasswordResetRequestDto("email-invalido");

        // When/Then
        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve confirmar reset com token válido")
    void shouldConfirmResetWithValidToken() throws Exception {
        // Given
        Usuario usuario = criarUsuarioTeste("user@test.com");
        String token = criarTokenValido(usuario);
        
        PasswordResetConfirmDto request = PasswordResetConfirmDto.builder()
            .token(token)
            .newPassword("NewPass@123")
            .confirmPassword("NewPass@123")
            .build();

        // When/Then
        mockMvc.perform(post("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("sucesso")));
    }

    @Test
    @DisplayName("Deve validar token corretamente")
    void shouldValidateTokenCorrectly() throws Exception {
        // Given
        Usuario usuario = criarUsuarioTeste("user@test.com");
        String token = criarTokenValido(usuario);

        // When/Then
        mockMvc.perform(get("/api/auth/password-reset/validate-token/" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.expired").value(false));
    }

    // Mais testes...
}
```

---

## 12. Critérios de Aceitação

### ✅ Funcionalidade

- [ ] **Campo email adicionado** ao Usuario sem quebrar código existente
- [ ] **Solicitação de reset** funciona para emails existentes
- [ ] **Resposta padronizada** mesmo para emails inexistentes
- [ ] **Email enviado** com link clicável e token fallback
- [ ] **Token expira** após 30 minutos
- [ ] **Token é único** (apenas 1 ativo por usuário)
- [ ] **Reset funciona** com token válido
- [ ] **Email de confirmação** enviado após reset bem-sucedido
- [ ] **Sessão atual** é encerrada após reset

### ✅ Segurança

- [ ] **Tokens hasheados** (nunca plain text no banco)
- [ ] **Rate limiting** funcionando (3/hora por IP e email)
- [ ] **Anti-enumeração** implementado (mesma resposta sempre)
- [ ] **Timing attack** mitigado (delay artificial)
- [ ] **Auditoria completa** de todas as tentativas
- [ ] **Senha forte** validada no reset

### ✅ Integração

- [ ] **Formato ApiResponse** implementado em todos os endpoints
- [ ] **Frontend pode consumir** os novos endpoints
- [ ] **CORS configurado** corretamente
- [ ] **Cookies funcionam** entre frontend e backend

### ✅ Internacionalização

- [ ] **Português (pt-BR)** totalmente funcional
- [ ] **Inglês (en-US)** totalmente funcional
- [ ] **Troca de idioma** baseada em Accept-Language
- [ ] **Templates de email** em ambos os idiomas

### ✅ Testes

- [ ] **Mínimo 12 testes unitários** passando
- [ ] **Mínimo 8 testes de integração** passando
- [ ] **Cobertura > 80%** nos serviços principais
- [ ] **Testes de segurança** (rate limit, anti-enumeração)

### ✅ Documentação

- [ ] **Postman Collection** atualizada com novos endpoints
- [ ] **README.md** atualizado
- [ ] **Guia de integração** frontend criado
- [ ] **Esta especificação** implementada 100%

### ✅ Qualidade

- [ ] **Zero linter errors**
- [ ] **Zero warnings de compilação**
- [ ] **Código revisado** (self-review + pair review se possível)
- [ ] **Princípios do projeto** seguidos rigorosamente

---

## 📝 Notas Finais

Esta especificação técnica serve como **contrato** entre planejamento e implementação. Todas as decisões arquiteturais foram tomadas seguindo os paradigmas do projeto:

- 🎯 **Gradualidade**: Implementação incremental e testável
- 📈 **Escalabilidade**: Preparado para crescer (SMTP agnóstico, i18n extensível)
- 🔧 **Extensibilidade**: Fácil adicionar idiomas, templates, proteções
- 🛡️ **Conservadorismo**: Mudanças mínimas ao código existente (Fase 1 intacta)
- 🎨 **Minimamente Invasivo**: Campo email adicionado sem remover username

**Próximo passo:** Iniciar implementação seguindo exatamente este plano.

---

**Preparado por:** Equipe Neuroefficiency  
**Aprovado por:** Rafael (Product Owner)  
**Data de Aprovação:** 14 de Outubro de 2025  
**Versão:** 1.0 - Final


