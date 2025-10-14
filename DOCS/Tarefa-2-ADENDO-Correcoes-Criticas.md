# 📌 ADENDO - Correções Críticas
## Tarefa 2: Recuperação de Senha por E-mail

**Data:** 14 de Outubro de 2025  
**Versão:** 1.1  
**Status:** ✅ **CORRIGIDO - Ler antes de implementar**  
**Complementa:** `Tarefa-2-Recuperacao-Senha-Email-Especificacao-Tecnica.md`

---

## ⚠️ ATENÇÃO

**Este documento DEVE ser lido ANTES de iniciar a implementação da Tarefa 2!**

A especificação técnica original contém **10 problemas críticos** que foram identificados e corrigidos. Este adendo lista as correções necessárias.

**Documento completo de correções:** `CORRECOES-E-AJUSTES-Tarefa-2.md`

---

## 🔴 CORREÇÕES CRÍTICAS (OBRIGATÓRIAS)

### 1️⃣ **TOKEN HASH: Usar SHA-256 ao invés de BCrypt** 🚨

**❌ NÃO FAZER (Especificação original):**
```java
// ISSO NÃO FUNCIONA!
PasswordResetToken resetToken = tokenRepository.findByTokenHash(
    passwordEncoder.encode(token)  // BCrypt usa salt aleatório!
).orElseThrow();
```

**✅ FAZER (Correto):**
```java
// Criar TokenUtils.java com SHA-256
String tokenHash = TokenUtils.hashToken(token);  // SHA-256
PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
    .orElseThrow();
```

**Por quê:** BCrypt gera hashes diferentes a cada chamada. SHA-256 é determinístico.

**Ver:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção 1 (detalhes completos)

---

### 2️⃣ **PORTA DO BACKEND: 8082** 

**✅ Ajustar:**
```properties
# application.properties
server.port=8082  # (não 8081)
```

**Ver:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção 2

---

### 3️⃣ **ApiResponse: Aplicar APENAS em novos endpoints**

**✅ Estratégia:**
- Criar `ApiResponse<T>` wrapper
- Aplicar **APENAS** em `/api/auth/password-reset/*`
- **MANTER** formato antigo nos endpoints existentes (login, register, etc.)

**Ver:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção 3

---

### 4️⃣ **RegisterRequest: Adicionar campo email**

**✅ Adicionar:**
```java
@NotBlank
@Email
private String email;
```

**Ver:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção 4

---

### 5️⃣ **UserResponse: Adicionar campo email**

**✅ Adicionar:**
```java
private String email;
```

**Ver:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção 5

---

### 6️⃣ **SecurityConfig: Endpoints públicos**

**✅ Adicionar:**
```java
.requestMatchers(
    "/api/auth/register",
    "/api/auth/login",
    "/api/auth/health",
    "/api/auth/password-reset/**"  // ⬅️ NOVO
).permitAll()
```

**Ver:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção 6

---

### 7️⃣ **Thymeleaf Dependency**

**✅ Adicionar ao pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

**Ver:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção 7

---

### 8️⃣ **MessageSource Configuration**

**✅ Criar:**
- `I18nConfig.java` com configuração de i18n

**Ver:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção 8

---

### 9️⃣ **@EnableScheduling**

**✅ Adicionar:**
```java
@SpringBootApplication
@EnableScheduling  // ⬅️ NOVO
public class NeuroefficiencyApplication { ... }
```

**Ver:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção 9

---

### 🔟 **validateToken(): Usar SHA-256**

**✅ Mesma correção do item #1**

**Ver:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção 10

---

## 📦 NOVAS CLASSES NECESSÁRIAS

### Utilitários
- ✅ `TokenUtils.java` - Geração e hash SHA-256 de tokens

### Configuração
- ✅ `I18nConfig.java` - Configuração de internacionalização

### DTOs
- ✅ `ApiResponse<T>` - Wrapper padronizado

### Exceptions
- ✅ `InvalidTokenException`
- ✅ `TokenExpiredException`
- ✅ `RateLimitExceededException`
- ✅ `EmailSendingException`
- ✅ `EmailAlreadyExistsException`

### Enums
- ✅ `AuditEventType`

**Ver implementações completas em:** `CORRECOES-E-AJUSTES-Tarefa-2.md`

---

## 📋 DEPENDÊNCIAS ADICIONAIS (pom.xml)

```xml
<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Thymeleaf para templates de email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- Apache Commons Codec (SHA-256) -->
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
</dependency>
```

---

## 🔧 AJUSTES NA ESPECIFICAÇÃO ORIGINAL

### Seção 6.2 - PasswordResetService

**Linhas 889-893 e 938-940:**

❌ **Remover:**
```java
passwordEncoder.encode(token)
```

✅ **Substituir por:**
```java
TokenUtils.hashToken(token)
```

### Seção 5.2 - Migration V3

**Linha 439:**

❌ **Remover:**
```sql
token_hash VARCHAR(255)
```

✅ **Substituir por:**
```sql
token_hash VARCHAR(64) NOT NULL UNIQUE  -- SHA-256 = 64 chars hex
```

**Adicionar comentário:**
```sql
COMMENT ON COLUMN password_reset_tokens.token_hash IS 'Hash SHA-256 do token (não BCrypt - precisa lookup direto)';
```

### Seção 5.4 - Usuario.java

**Linha 509:**

✅ **Email pode ser nullable** (para usuários legacy da Fase 1)

```java
@Email
@Column(unique = true, length = 255)  // nullable = true (padrão)
private String email;
```

---

## 🛠️ GUIA RÁPIDO DE INSTALAÇÃO - MailHog

### Docker (Recomendado):
```bash
docker run -d --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

### Acesso:
- **SMTP:** `localhost:1025`
- **Web UI:** `http://localhost:8025`

**Ver guia completo em:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção "MailHog Installation Guide"

---

## 🌍 VARIÁVEIS DE AMBIENTE

### Desenvolvimento
```properties
SERVER_PORT=8082
SMTP_HOST=localhost
SMTP_PORT=1025
FRONTEND_URL=http://localhost:5173
SPRING_PROFILES_ACTIVE=dev
```

### Produção
```properties
SERVER_PORT=8082
SMTP_HOST=${SMTP_HOST}
SMTP_PORT=${SMTP_PORT}
SMTP_USERNAME=${SMTP_USERNAME}
SMTP_PASSWORD=${SMTP_PASSWORD}
FRONTEND_URL=https://app.neuroefficiency.com
SPRING_PROFILES_ACTIVE=prod
```

**Ver lista completa em:** `CORRECOES-E-AJUSTES-Tarefa-2.md` seção "Environment Variables"

---

## ✅ CHECKLIST PRÉ-IMPLEMENTAÇÃO

Antes de começar a codificar, certifique-se de:

- [ ] Leu este adendo completamente
- [ ] Leu o documento completo de correções (`CORRECOES-E-AJUSTES-Tarefa-2.md`)
- [ ] Entendeu o problema do Token Hash (SHA-256 vs BCrypt)
- [ ] Tem MailHog instalado e rodando
- [ ] Conhece todas as novas classes necessárias
- [ ] Revisou o checklist de implementação atualizado

---

## 📚 DOCUMENTOS RELACIONADOS

1. **Especificação Original:** `Tarefa-2-Recuperacao-Senha-Email-Especificacao-Tecnica.md`
2. **Análise Completa:** `REVISAO-ANALISE-COMPLETA-Tarefa-2.md`
3. **Correções Detalhadas:** `CORRECOES-E-AJUSTES-Tarefa-2.md` ⭐ **LEITURA OBRIGATÓRIA**
4. **Este Adendo:** `Tarefa-2-ADENDO-Correcoes-Criticas.md`

---

## 🎯 ORDEM DE LEITURA RECOMENDADA

Para implementar a Tarefa 2 corretamente:

1. ✅ Ler este adendo (você está aqui) - **5 minutos**
2. ✅ Ler `CORRECOES-E-AJUSTES-Tarefa-2.md` completo - **15 minutos**
3. ✅ Revisar especificação original com as correções em mente - **10 minutos**
4. ✅ Seguir o checklist de implementação - **9 dias de trabalho**

---

## 💡 RESUMO EXECUTIVO

**O que mudou?**
- 🔧 Token hash: SHA-256 ao invés de BCrypt
- 🔧 Porta: 8082 (alinhado com frontend)
- 🔧 ApiResponse: Apenas em novos endpoints
- 🔧 Email: Adicionado em DTOs e entidades
- 🔧 Configurações: I18n, @EnableScheduling, SecurityConfig
- 🔧 Dependências: Thymeleaf, Mail, Commons Codec

**Por que mudou?**
- 🐛 Corrigir bugs críticos (token hash não funcionaria)
- 🔒 Garantir segurança adequada
- 📐 Alinhar frontend e backend
- 🎯 Seguir paradigmas do projeto

**Impacto:**
- ✅ **POSITIVO:** Código vai funcionar corretamente
- ✅ **SEM QUEBRAS:** Fase 1 permanece intacta
- ✅ **GRADUAL:** Mudanças mínimas e controladas

---

## ⚡ TL;DR

**Principais correções:**
1. Usar SHA-256 para tokens (não BCrypt)
2. Porta 8082
3. Criar TokenUtils, I18nConfig, ApiResponse
4. Adicionar email em RegisterRequest e UserResponse
5. Atualizar SecurityConfig

**Ação:** Ler `CORRECOES-E-AJUSTES-Tarefa-2.md` antes de começar!

---

**Preparado por:** AI Assistant  
**Data:** 14 de Outubro de 2025  
**Versão:** 1.1 - Adendo Crítico  
**Status:** ✅ Aprovado para Implementação com Correções


