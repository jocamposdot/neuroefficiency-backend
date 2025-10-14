# 📘 TAREFA 2 - REFERÊNCIA TÉCNICA
## Recuperação de Senha por Email - Decisões e Implementação

**Versão:** 1.0  
**Data de Implementação:** 14 de Outubro de 2025  
**Status:** ✅ Completo e Testado

---

## 🎯 OBJETIVO

Implementar sistema de recuperação de senha por email com:
- ✅ Segurança robusta (rate limiting, anti-enum, audit)
- ✅ Emails profissionais (HTML + texto, i18n)
- ✅ Escalabilidade e extensibilidade
- ✅ Minimal

amente invasivo (sem quebrar Fase 1)

---

## 🏗️ DECISÕES ARQUITETURAIS

### 1. Token: SHA-256 vs BCrypt

**Problema:** Qual algoritmo usar para hash de tokens de reset?

**Decisão:** **SHA-256** ✅

**Justificativa:**
- BCrypt é **não-determinístico** (gera hash diferente cada vez)
- Impossível fazer lookup no banco com BCrypt
- SHA-256 é **determinístico** (mesmo input = mesmo hash)
- Permite busca direta: `findByTokenHash(sha256(token))`

**Implementação:**
```java
// util/TokenUtils.java
public static String hashToken(String token) {
    return DigestUtils.sha256Hex(token);
}
```

**Para senhas de usuário:** Continua usando BCrypt (força 12)

---

### 2. Campo Email: Obrigatório ou Opcional?

**Problema:** Email deve ser obrigatório no registro?

**Decisão:** **Opcional para usuários legacy, obrigatório para novos** ✅

**Justificativa:**
- **Minimalamente invasivo** - não quebra usuários da Fase 1
- **Permite migração gradual**
- **Extensível** para futuras features (verificação de email)

**Implementação:**
```sql
-- Migration V2
ALTER TABLE usuarios ADD COLUMN email VARCHAR(255);
CREATE UNIQUE INDEX uk_usuarios_email ON usuarios(email);
```

```java
@Email
@Size(max = 255)
@Column(unique = true)
private String email;
```

---

### 3. Índices: Partial vs Standard

**Problema:** H2 não suporta partial indexes (`WHERE email IS NOT NULL`)

**Decisão:** **Standard UNIQUE INDEX** ✅

**Justificativa:**
- H2 já permite múltiplos NULL em UNIQUE INDEX
- Compatível com H2 e PostgreSQL
- Mais simples e direto

**Bug Encontrado:**
```sql
-- ERRADO (H2 não suporta)
CREATE UNIQUE INDEX uk_usuarios_email ON usuarios(email) 
WHERE email IS NOT NULL;

-- CORRETO
CREATE UNIQUE INDEX uk_usuarios_email ON usuarios(email);
```

---

### 4. API Response: Padrão Global ou Específico?

**Problema:** Unificar formato de resposta da API?

**Decisão:** **Novo formato apenas para endpoints de password reset** ✅

**Justificativa:**
- **Conservadorismo** - não quebra frontend da Fase 1
- **Extensível** - novos endpoints podem usar novo formato
- **Backward compatible**

**Implementação:**
```java
// Novo formato (apenas password reset)
{
  "success": true,
  "data": { ... },
  "message": "..."
}

// Formato antigo (auth endpoints)
{
  "message": "...",
  "user": { ... }
}
```

---

### 5. Email Service: SMTP Real ou Mock?

**Problema:** Como testar emails localmente?

**Decisão:** **MailHog para dev, SMTP real para prod** ✅

**Justificativa:**
- **MailHog** permite testar sem spam
- **Agnóstico** - fácil trocar para SendGrid/AWS SES
- **Profile-based** configuration

**Configuração:**
```properties
# Dev
spring.mail.host=localhost
spring.mail.port=1025

# Prod
spring.mail.host=smtp.sendgrid.net
spring.mail.username=apikey
spring.mail.password=${SENDGRID_API_KEY}
```

---

## 🔒 SEGURANÇA IMPLEMENTADA

### 1. Rate Limiting

**Limite:** 3 tentativas por hora por email/IP

**Implementação:**
```java
private void checkRateLimit(String email, String ipAddress) {
    long emailAttempts = auditRepository
        .countByEmailAndTimestampAfter(email, oneHourAgo);
    
    long ipAttempts = auditRepository
        .countByIpAddressAndTimestampAfter(ipAddress, oneHourAgo);
    
    if (emailAttempts >= 3 || ipAttempts >= 3) {
        throw new RateLimitExceededException();
    }
}
```

**Teste:**
```bash
# 4ª tentativa retorna 429
```

---

### 2. Anti-Enumeração

**Problema:** Não revelar se email existe no sistema

**Implementação:**
```java
// Sempre retorna 200 OK
if (usuario == null) {
    simulateDelay(); // 500-1000ms
    logAudit(email, "Email não encontrado (oculto)");
}
return ResponseEntity.ok(
    ApiResponse.success("Se o email existir, você receberá...")
);
```

**Resultado:**
- Email existe: 200 OK + email enviado
- Email não existe: 200 OK + delay artificial

---

### 3. Token de Uso Único

**Implementação:**
```java
// Ao confirmar reset
token.markAsUsed(); // set usedAt = now()
tokenRepository.save(token);

// Ao validar
if (token.isUsed()) {
    throw new TokenInvalidException("Token já foi usado");
}
```

**Verificação:**
```sql
SELECT used_at FROM password_reset_tokens WHERE id = 1;
-- used_at != NULL significa token usado
```

---

### 4. Expiração de Tokens

**Tempo:** 30 minutos

**Implementação:**
```java
PasswordResetToken.builder()
    .expiresAt(LocalDateTime.now().plusMinutes(30))
    .build();

// Validação
public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
}
```

**Cleanup Job:**
```java
@Scheduled(cron = "0 0 3 * * *") // 3h da manhã
public void cleanupExpiredTokens() {
    tokenRepository.deleteExpiredTokens(LocalDateTime.now());
}
```

---

### 5. Auditoria LGPD

**Dados Registrados:**
- Email
- IP Address
- User-Agent
- Event Type (REQUEST, SUCCESS, RATE_LIMIT, etc.)
- Timestamp
- Success/Failure
- Error Message (se houver)

**Implementação:**
```java
private void logAudit(String email, String ipAddress, 
                      String userAgent, AuditEventType eventType,
                      boolean success, String errorMessage) {
    PasswordResetAudit audit = PasswordResetAudit.builder()
        .email(sanitizeEmail(email))
        .ipAddress(ipAddress)
        .userAgent(userAgent)
        .eventType(eventType)
        .success(success)
        .errorMessage(errorMessage)
        .build();
    
    auditRepository.save(audit);
}
```

**Query para análise:**
```sql
SELECT * FROM password_reset_audit 
WHERE timestamp > DATEADD('DAY', -7, CURRENT_TIMESTAMP)
ORDER BY timestamp DESC;
```

---

## 📧 EMAILS

### Templates Multipart

**Formato:** HTML + Texto Simples

**Justificativa:**
- HTML: Melhor UX, profissional
- Texto: Fallback para clientes antigos

**Tecnologia:** Thymeleaf

**Implementação:**
```java
MimeMessage message = mailSender.createMimeMessage();
MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

String htmlContent = templateEngine.process("email/password-reset", context);
String textContent = templateEngine.process("email/password-reset", context);

helper.setText(textContent, htmlContent);
```

---

### Internacionalização (i18n)

**Idiomas:** pt-BR (padrão), en-US

**Implementação:**
```java
@Configuration
public class I18nConfig {
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }
}
```

**Uso:**
```java
String subject = messageSource.getMessage(
    "email.password-reset.subject",
    null,
    locale
);
```

**Headers:**
```http
Accept-Language: pt-BR  # Português
Accept-Language: en-US  # Inglês
```

---

## 🗄️ BANCO DE DADOS

### Tabelas Criadas

#### 1. usuarios (modificada)
```sql
ALTER TABLE usuarios ADD COLUMN email VARCHAR(255);
CREATE UNIQUE INDEX uk_usuarios_email ON usuarios(email);
```

#### 2. password_reset_tokens (nova)
```sql
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) UNIQUE NOT NULL,
    usuario_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Índices
CREATE INDEX idx_password_reset_tokens_usuario ON password_reset_tokens(usuario_id);
CREATE INDEX idx_password_reset_tokens_expires ON password_reset_tokens(expires_at);
CREATE INDEX idx_password_reset_tokens_used ON password_reset_tokens(used_at);
```

#### 3. password_reset_audit (nova)
```sql
CREATE TABLE password_reset_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    event_type VARCHAR(50) NOT NULL,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    timestamp TIMESTAMP NOT NULL
);

-- Índices para rate limiting
CREATE INDEX idx_password_reset_audit_email_time ON password_reset_audit(email, timestamp);
CREATE INDEX idx_password_reset_audit_ip_time ON password_reset_audit(ip_address, timestamp);
CREATE INDEX idx_password_reset_audit_timestamp ON password_reset_audit(timestamp);
```

---

## 🌐 ENDPOINTS

### 1. POST /api/auth/password-reset/request
**Acesso:** Público  
**Rate Limit:** 3/hora por email/IP

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:** 200 OK (sempre)
```json
{
  "success": true,
  "message": "Se o email existir, você receberá instruções..."
}
```

---

### 2. GET /api/auth/password-reset/validate-token/{token}
**Acesso:** Público

**Response:** 200 OK
```json
{
  "success": true,
  "data": { "valid": true },
  "message": "Token válido"
}
```

---

### 3. POST /api/auth/password-reset/confirm
**Acesso:** Público

**Request:**
```json
{
  "token": "64_char_token",
  "newPassword": "NewPass@1234",
  "confirmPassword": "NewPass@1234"
}
```

**Response:** 200 OK
```json
{
  "success": true,
  "message": "Senha redefinida com sucesso!"
}
```

---

### 4. GET /api/auth/password-reset/health
**Acesso:** Público

**Response:** 200 OK
```json
{
  "success": true,
  "data": {
    "version": "1.0",
    "status": "UP",
    "service": "password-reset"
  },
  "message": "Serviço de recuperação de senha operacional"
}
```

---

## 🐛 PROBLEMAS RESOLVIDOS

### Bug #1: H2 Partial Indexes
**Problema:** `Syntax error` com `WHERE email IS NOT NULL`

**Solução:** Remover cláusula WHERE
```sql
-- Antes (erro)
CREATE UNIQUE INDEX uk_usuarios_email ON usuarios(email) WHERE email IS NOT NULL;

-- Depois (correto)
CREATE UNIQUE INDEX uk_usuarios_email ON usuarios(email);
```

---

### Bug #2: TIMESTAMP Syntax
**Problema:** H2 não reconhece `TIMESTAMP WITHOUT TIME ZONE`

**Solução:** Usar apenas `TIMESTAMP`
```sql
-- Antes (erro)
expires_at TIMESTAMP WITHOUT TIME ZONE

-- Depois (correto)
expires_at TIMESTAMP
```

---

### Problema #1: BCrypt Token Lookup
**Problema:** `findByTokenHash(bcrypt(token))` não funciona

**Solução:** Usar SHA-256 (determinístico)

---

### Problema #2: Port Mismatch
**Problema:** Backend 8081 vs Frontend 8082

**Solução:** `application-dev.properties` com `server.port=8082`

---

### Problema #3: API Response Incompatível
**Problema:** Frontend espera `{success, data, message}`

**Solução:** Novo formato apenas para novos endpoints

---

## 📊 MÉTRICAS

### Código
- **Classes:** 30 (+16 da Fase 1)
- **Linhas:** ~3.700
- **Endpoints:** 9 (5 + 4)
- **Migrations:** 4 (V1-V4)

### Segurança
- **Rate Limiting:** 3/hora
- **Token Expiration:** 30min
- **Hash Algorithm:** SHA-256 (tokens), BCrypt (senhas)
- **Audit Events:** 7 tipos

### Documentação
- **Arquivos:** 8 ativos
- **Linhas:** ~3.500
- **Cobertura:** 100%

---

## 🔄 FLUXO COMPLETO

```
1. Usuário solicita reset
   └─> POST /password-reset/request
   
2. Sistema valida rate limiting
   └─> Verifica tentativas (email/IP)
   
3. Sistema busca usuário
   └─> findByEmail(email)
   
4. Sistema gera token
   ├─> Token bruto (64 chars hex)
   └─> Hash SHA-256 para banco
   
5. Sistema salva token
   ├─> expires_at = now + 30min
   └─> used_at = null
   
6. Sistema envia email
   ├─> Template Thymeleaf
   ├─> Multipart (HTML + texto)
   └─> i18n (pt-BR/en-US)
   
7. Usuário recebe email
   └─> Clica no link com token
   
8. Frontend valida token
   └─> GET /validate-token/{token}
   
9. Usuário confirma nova senha
   └─> POST /confirm
   
10. Sistema valida token
    ├─> findByTokenHash(sha256(token))
    ├─> Verifica expiração
    └─> Verifica se já foi usado
    
11. Sistema atualiza senha
    ├─> BCrypt hash da nova senha
    └─> save(usuario)
    
12. Sistema invalida token
    └─> markAsUsed() → used_at = now
    
13. Sistema envia confirmação
    └─> Email "Senha alterada"
    
14. Sistema registra auditoria
    └─> Todos eventos salvos
```

---

## 📚 REFERÊNCIAS

### Código Principal
- `PasswordResetService.java` - Lógica de negócio
- `EmailService.java` - Envio de emails
- `PasswordResetController.java` - Endpoints REST
- `TokenUtils.java` - Geração e hash de tokens

### Configurações
- `application-dev.properties` - SMTP MailHog
- `messages_*.properties` - i18n
- `I18nConfig.java` - MessageSource

### Migrations
- `V2__add_email_to_usuarios.sql`
- `V3__create_password_reset_tokens.sql`
- `V4__create_password_reset_audit.sql`

---

**Documentação Completa:** Ver `DOCS/CHANGELOG.md` para histórico detalhado.

✅ **Referência Técnica Completa!**

