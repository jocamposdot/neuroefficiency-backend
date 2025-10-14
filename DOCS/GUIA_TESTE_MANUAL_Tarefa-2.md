# 🧪 GUIA DE TESTE MANUAL - Recuperação de Senha
## Tarefa 2: Reset de Senha por Email

**Data:** 14 de Outubro de 2025  
**Versão:** 1.0  
**Status:** Pronto para testes

---

## 📋 PRÉ-REQUISITOS

Antes de começar os testes, certifique-se de que:

- [ ] MailHog está rodando (`docker run -d --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog`)
- [ ] Backend está rodando (`./mvnw spring-boot:run`)
- [ ] Frontend está rodando (opcional, mas recomendado)
- [ ] Postman ou similar para testar API
- [ ] Navegador aberto em `http://localhost:8025` (MailHog UI)

---

## 🎯 CENÁRIOS DE TESTE

### CENÁRIO 1: Fluxo Completo de Sucesso ✅

**Objetivo:** Testar o fluxo completo de recuperação de senha.

#### Passo 1: Criar usuário com email

```bash
POST http://localhost:8082/api/auth/register
Content-Type: application/json

{
  "username": "testeuser",
  "email": "teste@example.com",
  "password": "Test@1234",
  "confirmPassword": "Test@1234"
}
```

**Resultado esperado:**
- Status: 200 OK
- Response:
```json
{
  "message": "Usuário registrado com sucesso",
  "user": {
    "id": 1,
    "username": "testeuser",
    "email": "teste@example.com",
    "enabled": true,
    "createdAt": "2025-10-14T20:30:00"
  }
}
```

✅ **Verificar:**
- [ ] Usuário criado com sucesso
- [ ] Email salvo no banco
- [ ] Response contém o email

---

#### Passo 2: Solicitar reset de senha

```bash
POST http://localhost:8082/api/auth/password-reset/request
Content-Type: application/json
Accept-Language: pt-BR

{
  "email": "teste@example.com"
}
```

**Resultado esperado:**
- Status: 200 OK
- Response:
```json
{
  "success": true,
  "data": null,
  "message": "Se o email existir, você receberá instruções para redefinir sua senha."
}
```

✅ **Verificar:**
- [ ] Response sempre 200 OK (mesmo se email não existir)
- [ ] Mensagem genérica (anti-enumeração)

---

#### Passo 3: Verificar email no MailHog

1. Abrir: `http://localhost:8025`
2. Verificar novo email recebido

✅ **Verificar:**
- [ ] Email apareceu no MailHog
- [ ] Assunto: "Redefinir sua senha - Neuroefficiency"
- [ ] Remetente: `noreply@neuroefficiency.local`
- [ ] Destinatário: `teste@example.com`
- [ ] Email tem versão HTML (bonita)
- [ ] Email tem versão texto (fallback)
- [ ] Link de reset presente: `http://localhost:5173/#/reset-password?token=...`
- [ ] Token tem 64 caracteres hexadecimais
- [ ] Mensagem em português (Accept-Language: pt-BR)

**Copiar o token do email para próximo passo!**

---

#### Passo 4: Validar token (opcional)

```bash
GET http://localhost:8082/api/auth/password-reset/validate-token/{TOKEN_AQUI}
```

**Resultado esperado:**
- Status: 200 OK
- Response:
```json
{
  "success": true,
  "data": {
    "valid": true
  },
  "message": "Token válido"
}
```

✅ **Verificar:**
- [ ] Token reconhecido como válido
- [ ] Response com `"valid": true`

---

#### Passo 5: Confirmar reset de senha

```bash
POST http://localhost:8082/api/auth/password-reset/confirm
Content-Type: application/json
Accept-Language: pt-BR

{
  "token": "TOKEN_COPIADO_DO_EMAIL",
  "newPassword": "NewPass@1234",
  "confirmPassword": "NewPass@1234"
}
```

**Resultado esperado:**
- Status: 200 OK
- Response:
```json
{
  "success": true,
  "data": null,
  "message": "Senha redefinida com sucesso! Você já pode fazer login com sua nova senha."
}
```

✅ **Verificar:**
- [ ] Senha alterada com sucesso
- [ ] Email de confirmação enviado

---

#### Passo 6: Verificar email de confirmação

1. Voltar ao MailHog: `http://localhost:8025`
2. Verificar novo email

✅ **Verificar:**
- [ ] Segundo email recebido
- [ ] Assunto: "Senha alterada com sucesso - Neuroefficiency"
- [ ] Contém data/hora da alteração
- [ ] Aviso de segurança presente

---

#### Passo 7: Fazer login com nova senha

```bash
POST http://localhost:8082/api/auth/login
Content-Type: application/json

{
  "username": "testeuser",
  "password": "NewPass@1234"
}
```

**Resultado esperado:**
- Status: 200 OK
- Login bem-sucedido

✅ **Verificar:**
- [ ] Login funciona com nova senha
- [ ] Senha antiga NÃO funciona mais

---

### CENÁRIO 2: Rate Limiting 🚫

**Objetivo:** Testar proteção contra abuso (3 tentativas/hora).

#### Fazer 4 solicitações seguidas:

```bash
# Tentativa 1
POST http://localhost:8082/api/auth/password-reset/request
{"email": "teste@example.com"}

# Tentativa 2
POST http://localhost:8082/api/auth/password-reset/request
{"email": "teste@example.com"}

# Tentativa 3
POST http://localhost:8082/api/auth/password-reset/request
{"email": "teste@example.com"}

# Tentativa 4 (deve falhar)
POST http://localhost:8082/api/auth/password-reset/request
{"email": "teste@example.com"}
```

**Resultado esperado (tentativa 4):**
- Status: 429 TOO_MANY_REQUESTS
- Response:
```json
{
  "timestamp": "2025-10-14T20:35:00",
  "status": 429,
  "error": "Rate limit excedido",
  "message": "Limite de 3 tentativas por hora excedido. Tente novamente mais tarde."
}
```

✅ **Verificar:**
- [ ] 3 primeiras tentativas: 200 OK
- [ ] 4ª tentativa: 429 TOO_MANY_REQUESTS
- [ ] Mensagem clara sobre rate limit
- [ ] Auditoria registrada no banco

---

### CENÁRIO 3: Anti-Enumeração 🕵️

**Objetivo:** Verificar que não revelamos se email existe.

#### Testar com email inexistente:

```bash
POST http://localhost:8082/api/auth/password-reset/request
Content-Type: application/json

{
  "email": "naoexiste@example.com"
}
```

**Resultado esperado:**
- Status: 200 OK (MESMO sem email existir!)
- Response IDÊNTICA ao caso de sucesso:
```json
{
  "success": true,
  "data": null,
  "message": "Se o email existir, você receberá instruções para redefinir sua senha."
}
```

✅ **Verificar:**
- [ ] Response 200 OK (não 404!)
- [ ] Mensagem genérica (não revela se email existe)
- [ ] Tempo de resposta similar (~500-1000ms devido ao delay artificial)
- [ ] Nenhum email enviado (verificar MailHog)
- [ ] Auditoria registrada no banco

**IMPORTANTE:** Atacante NÃO consegue descobrir se email está cadastrado!

---

### CENÁRIO 4: Token Expirado ⏰

**Objetivo:** Testar expiração de token (30 minutos).

**Opção A - Teste Rápido (Mock):**
1. Gerar token
2. Alterar `expires_at` no banco para data passada:
```sql
UPDATE password_reset_tokens 
SET expires_at = NOW() - INTERVAL '1 hour' 
WHERE id = (SELECT MAX(id) FROM password_reset_tokens);
```
3. Tentar usar o token

**Opção B - Teste Real:**
1. Gerar token
2. Aguardar 31 minutos
3. Tentar usar o token

**Resultado esperado:**
- Status: 410 GONE
- Response:
```json
{
  "timestamp": "2025-10-14T21:00:00",
  "status": 410,
  "error": "Token expirado",
  "message": "Token de reset de senha expirou. Solicite um novo token."
}
```

✅ **Verificar:**
- [ ] Token rejeitado
- [ ] Status 410 GONE (não 400)
- [ ] Mensagem clara sobre expiração
- [ ] Auditoria: EXPIRED_TOKEN

---

### CENÁRIO 5: Token Inválido ❌

**Objetivo:** Testar token inexistente ou já usado.

#### 5.1 Token inexistente:

```bash
POST http://localhost:8082/api/auth/password-reset/confirm
Content-Type: application/json

{
  "token": "0000000000000000000000000000000000000000000000000000000000000000",
  "newPassword": "NewPass@1234",
  "confirmPassword": "NewPass@1234"
}
```

**Resultado esperado:**
- Status: 400 BAD_REQUEST
- Response:
```json
{
  "timestamp": "2025-10-14T20:40:00",
  "status": 400,
  "error": "Token inválido",
  "message": "Token de reset de senha inválido ou já foi usado."
}
```

#### 5.2 Token já usado:

1. Usar token uma vez (sucesso)
2. Tentar usar o mesmo token novamente

**Resultado esperado:**
- Status: 400 BAD_REQUEST
- Mensagem: "Token já foi usado"

✅ **Verificar:**
- [ ] Token rejeitado
- [ ] Mensagem apropriada
- [ ] Auditoria: INVALID_TOKEN

---

### CENÁRIO 6: Senhas Não Coincidem 🔐

**Objetivo:** Testar validação de confirmação de senha.

```bash
POST http://localhost:8082/api/auth/password-reset/confirm
Content-Type: application/json

{
  "token": "TOKEN_VALIDO",
  "newPassword": "NewPass@1234",
  "confirmPassword": "DifferentPass@1234"
}
```

**Resultado esperado:**
- Status: 400 BAD_REQUEST
- Response:
```json
{
  "timestamp": "2025-10-14T20:45:00",
  "status": 400,
  "error": "Senhas não coincidem",
  "message": "As senhas não coincidem."
}
```

✅ **Verificar:**
- [ ] Request rejeitado
- [ ] Senha não alterada
- [ ] Mensagem clara

---

### CENÁRIO 7: Validações de Senha 🔒

**Objetivo:** Testar requisitos mínimos de senha.

Testar senhas inválidas:

```bash
# Senha muito curta
{"newPassword": "Ab1!", "confirmPassword": "Ab1!"}

# Sem maiúscula
{"newPassword": "abcd1234!", "confirmPassword": "abcd1234!"}

# Sem minúscula
{"newPassword": "ABCD1234!", "confirmPassword": "ABCD1234!"}

# Sem número
{"newPassword": "AbcdEfgh!", "confirmPassword": "AbcdEfgh!"}

# Sem caractere especial
{"newPassword": "Abcd1234", "confirmPassword": "Abcd1234"}
```

**Resultado esperado:**
- Status: 400 BAD_REQUEST
- Response com erros de validação

✅ **Verificar:**
- [ ] Todas as validações funcionam
- [ ] Mensagens claras de erro

---

### CENÁRIO 8: Internacionalização 🌐

**Objetivo:** Testar emails em inglês.

```bash
POST http://localhost:8082/api/auth/password-reset/request
Content-Type: application/json
Accept-Language: en-US

{
  "email": "teste@example.com"
}
```

✅ **Verificar no MailHog:**
- [ ] Assunto em inglês: "Reset your password - Neuroefficiency"
- [ ] Conteúdo em inglês
- [ ] Botão: "Reset Password"

---

### CENÁRIO 9: Múltiplos Tokens do Mesmo Usuário 🔄

**Objetivo:** Testar invalidação de tokens antigos.

1. Solicitar token #1 para usuário
2. Solicitar token #2 para mesmo usuário
3. Tentar usar token #1 (deve estar invalidado)
4. Usar token #2 (deve funcionar)

✅ **Verificar:**
- [ ] Apenas 1 token ativo por usuário
- [ ] Tokens antigos invalidados automaticamente
- [ ] Token mais recente funciona

---

### CENÁRIO 10: Health Check 🏥

**Objetivo:** Testar endpoint de status.

```bash
GET http://localhost:8082/api/auth/password-reset/health
```

**Resultado esperado:**
- Status: 200 OK
- Response:
```json
{
  "success": true,
  "data": {
    "status": "UP",
    "service": "password-reset",
    "version": "1.0"
  },
  "message": "Serviço de recuperação de senha operacional"
}
```

✅ **Verificar:**
- [ ] Endpoint acessível
- [ ] Status UP
- [ ] Informações corretas

---

## 📊 VERIFICAÇÕES NO BANCO DE DADOS

### Verificar tabela password_reset_tokens:

```sql
SELECT 
    id, 
    LEFT(token_hash, 16) || '...' as token_hash_preview,
    usuario_id,
    expires_at,
    used_at,
    created_at
FROM password_reset_tokens
ORDER BY created_at DESC
LIMIT 5;
```

✅ **Verificar:**
- [ ] Tokens salvos com hash SHA-256
- [ ] expires_at = created_at + 30 minutos
- [ ] used_at NULL para tokens não usados
- [ ] used_at preenchido após uso

---

### Verificar tabela password_reset_audit:

```sql
SELECT 
    id,
    email,
    ip_address,
    event_type,
    success,
    error_message,
    timestamp
FROM password_reset_audit
ORDER BY timestamp DESC
LIMIT 10;
```

✅ **Verificar:**
- [ ] Todas tentativas registradas
- [ ] event_type correto (REQUEST, SUCCESS, FAILURE, etc.)
- [ ] IP capturado
- [ ] Timestamps corretos

---

### Verificar campo email em usuarios:

```sql
SELECT id, username, email, enabled, created_at
FROM usuarios
ORDER BY created_at DESC;
```

✅ **Verificar:**
- [ ] Novos usuários têm email
- [ ] Email em lowercase
- [ ] Email único

---

## 🐛 TROUBLESHOOTING

### Problema: Email não chega no MailHog

**Possíveis causas:**
1. MailHog não está rodando
2. Porta 1025 ocupada
3. Configuração SMTP incorreta

**Solução:**
```bash
# Verificar se MailHog está rodando
docker ps | findstr mailhog

# Ver logs do backend
# Procurar por: "Email de reset enviado com sucesso"

# Testar conexão SMTP manualmente
telnet localhost 1025
```

---

### Problema: 401 Unauthorized nos endpoints

**Causa:** SecurityConfig não liberou os endpoints.

**Solução:**
Verificar `SecurityConfig.java`:
```java
.requestMatchers("/api/auth/password-reset/**").permitAll()
```

---

### Problema: Token hash não encontra token no banco

**Causa:** Usando BCrypt ao invés de SHA-256.

**Solução:**
Verificar se `TokenUtils.hashToken()` usa `DigestUtils.sha256Hex()`.

---

### Problema: Rate limit não funciona

**Causa:** Job de auditoria não está salvando.

**Solução:**
Verificar logs e tabela `password_reset_audit`.

---

## ✅ CHECKLIST FINAL DE TESTES

### Funcionalidades Core:
- [ ] ✅ Solicitar reset de senha
- [ ] ✅ Receber email com token
- [ ] ✅ Validar token
- [ ] ✅ Confirmar reset com nova senha
- [ ] ✅ Login com nova senha funciona

### Segurança:
- [ ] 🚫 Rate limiting (3/hora)
- [ ] 🕵️ Anti-enumeração (resposta sempre igual)
- [ ] ⏰ Tokens expiram em 30min
- [ ] 🔒 Tokens uso único
- [ ] 📝 Auditoria completa

### Emails:
- [ ] 📧 Email de reset (multipart)
- [ ] 📧 Email de confirmação
- [ ] 🌐 i18n (pt-BR e en-US)
- [ ] 🎨 Templates HTML bonitos

### Validações:
- [ ] ✅ Email válido obrigatório
- [ ] ✅ Senha forte obrigatória
- [ ] ✅ Senhas devem coincidir
- [ ] ✅ Token formato correto

### Performance:
- [ ] ⚡ Delay artificial (anti-timing)
- [ ] 🧹 Job de limpeza de tokens
- [ ] 📊 Índices no banco

---

## 📝 RELATÓRIO DE BUGS

Se encontrar bugs durante os testes, documente aqui:

| # | Cenário | Bug Encontrado | Severidade | Status |
|---|---------|----------------|------------|--------|
| 1 |         |                |            |        |
| 2 |         |                |            |        |

---

**Preparado por:** Neuroefficiency Team  
**Data:** 14 de Outubro de 2025  
**Próxima etapa:** Testes automatizados após validação manual

