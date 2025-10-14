# 🧪 GUIA DE TESTES
## Testes Manuais e Automatizados

**Versão:** 3.0 (Fase 2)  
**Última Atualização:** 14 de Outubro de 2025  
**Cobertura:** 9 endpoints, 10 cenários E2E

---

## 🎯 VISÃO GERAL

### Scripts Disponíveis
| Script | Tempo | Descrição |
|--------|-------|-----------|
| `test-complete-auto.ps1` | ~30s | ⭐ Teste E2E completo automatizado |
| `test-fresh.ps1` | ~15s | Cria usuário novo (evita rate limit) |
| `test-simple.ps1` | ~10s | Teste básico rápido |
| `check-rate-limit.ps1` | ~5s | Verifica rate limiting no banco |

### Endpoints Testados
- ✅ 5 endpoints de autenticação (Fase 1)
- ✅ 4 endpoints de recuperação de senha (Fase 2)
- ✅ **Total:** 9 endpoints (100%)

---

## ⚡ TESTE RÁPIDO (RECOMENDADO)

### Pré-requisitos
```powershell
# 1. Backend rodando
.\mvnw.cmd spring-boot:run

# 2. MailHog rodando (em outro terminal)
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

### Executar Teste Completo
```powershell
.\test-complete-auto.ps1
```

**Resultado Esperado:**
```
✅ 1. Token extraído do email
✅ 2. Token validado
✅ 3. Senha alterada
✅ 4. Email de confirmação enviado
✅ 5. Token invalidado após uso
✅ 6. Login com nova senha
✅ 7. Senha antiga bloqueada
```

**Tempo:** ~30 segundos  
**Interação:** Zero (100% automatizado)

---

## 📋 10 CENÁRIOS DE TESTE

### Cenário 1: Criar Usuário ✅
```powershell
$registerData = @{
    username = "testuser"
    email = "test@example.com"
    password = "Test@1234"
    confirmPassword = "Test@1234"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/register" `
    -Method POST -ContentType "application/json" -Body $registerData
```

**Resultado Esperado:**
```json
{
  "message": "Usuário registrado com sucesso",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

---

### Cenário 2: Solicitar Reset de Senha ✅
```powershell
$resetData = @{ email = "test@example.com" } | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/request" `
    -Method POST -ContentType "application/json" `
    -Headers @{"Accept-Language" = "pt-BR"} -Body $resetData
```

**Resultado Esperado:**
```json
{
  "success": true,
  "message": "Se o email existir, você receberá instruções..."
}
```

**Verificar:**
- ✅ Email recebido no MailHog (http://localhost:8025)
- ✅ Resposta sempre 200 OK (anti-enumeração)

---

### Cenário 3: Validar Token ✅
```powershell
# Extrair token do email no MailHog
$token = "COLE_TOKEN_AQUI"

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/validate-token/$token" `
    -Method GET
```

**Resultado Esperado:**
```json
{
  "success": true,
  "data": { "valid": true },
  "message": "Token válido"
}
```

---

### Cenário 4: Confirmar Reset ✅
```powershell
$confirmData = @{
    token = $token
    newPassword = "NewPass@1234"
    confirmPassword = "NewPass@1234"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/confirm" `
    -Method POST -ContentType "application/json" `
    -Headers @{"Accept-Language" = "pt-BR"} -Body $confirmData
```

**Resultado Esperado:**
```json
{
  "success": true,
  "message": "Senha redefinida com sucesso!"
}
```

**Verificar:**
- ✅ 2º email de confirmação no MailHog

---

### Cenário 5: Login com Nova Senha ✅
```powershell
$loginData = @{
    username = "testuser"
    password = "NewPass@1234"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/login" `
    -Method POST -ContentType "application/json" -Body $loginData
```

**Resultado Esperado:**
```json
{
  "message": "Login realizado com sucesso",
  "user": { "username": "testuser", "email": "test@example.com" }
}
```

---

### Cenário 6: Senha Antiga Bloqueada ✅
```powershell
$oldLoginData = @{
    username = "testuser"
    password = "Test@1234"  # Senha antiga
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8082/api/auth/login" `
        -Method POST -ContentType "application/json" -Body $oldLoginData
    Write-Host "[ERRO] Senha antiga ainda funciona!" -ForegroundColor Red
} catch {
    Write-Host "[OK] Senha antiga bloqueada!" -ForegroundColor Green
}
```

**Resultado Esperado:** Erro 401 Unauthorized

---

### Cenário 7: Token Invalidado Após Uso ✅
```powershell
# Tentar validar token novamente
Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/validate-token/$token" `
    -Method GET
```

**Resultado Esperado:**
```json
{
  "success": true,
  "data": { "valid": false },
  "message": "Token inválido ou expirado"
}
```

---

### Cenário 8: Rate Limiting ✅
```powershell
# Fazer 4 tentativas de reset para o mesmo email
1..4 | ForEach-Object {
    try {
        Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/request" `
            -Method POST -ContentType "application/json" `
            -Body '{"email":"test@example.com"}'
        Write-Host "Tentativa $_ : OK" -ForegroundColor Green
    } catch {
        Write-Host "Tentativa $_ : BLOQUEADO (Rate Limit)" -ForegroundColor Yellow
    }
}
```

**Resultado Esperado:**
- Tentativas 1-3: ✅ 200 OK
- Tentativa 4: ❌ 429 Too Many Requests

**Verificar no banco:**
```sql
SELECT COUNT(*) FROM password_reset_audit 
WHERE email = 'test@example.com' 
AND timestamp > DATEADD('HOUR', -1, CURRENT_TIMESTAMP);
```

---

### Cenário 9: Anti-Enumeração ✅
```powershell
# Solicitar reset para email inexistente
$fakeData = @{ email = "naoexiste@example.com" } | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/request" `
    -Method POST -ContentType "application/json" -Body $fakeData
```

**Resultado Esperado:**
```json
{
  "success": true,
  "message": "Se o email existir, você receberá instruções..."
}
```

**Verificar:**
- ✅ Resposta idêntica ao email existente
- ✅ Delay artificial de ~500-1000ms
- ✅ Nenhum email enviado ao MailHog

---

### Cenário 10: Internacionalização ✅
```powershell
# Solicitar em inglês
$resetData = @{ email = "test@example.com" } | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/request" `
    -Method POST -ContentType "application/json" `
    -Headers @{"Accept-Language" = "en-US"} -Body $resetData
```

**Verificar no MailHog:**
- ✅ Assunto do email em inglês: "Reset your password - Neuroefficiency"
- ✅ Conteúdo em inglês
- ✅ Botão: "Reset Password"

---

## 🔍 VERIFICAÇÕES NO BANCO DE DADOS

### H2 Console
**URL:** http://localhost:8082/h2-console  
**JDBC URL:** `jdbc:h2:mem:neurodb`  
**User:** `sa`  
**Password:** (vazio)

### Queries Úteis

#### Ver Tokens Ativos
```sql
SELECT id, usuario_id, expires_at, used_at, created_at 
FROM password_reset_tokens 
WHERE used_at IS NULL 
AND expires_at > CURRENT_TIMESTAMP
ORDER BY created_at DESC;
```

#### Ver Auditoria de Reset
```sql
SELECT email, ip_address, event_type, success, timestamp
FROM password_reset_audit 
ORDER BY timestamp DESC 
LIMIT 10;
```

#### Contar Tentativas (Rate Limiting)
```sql
SELECT ip_address, COUNT(*) as tentativas
FROM password_reset_audit 
WHERE timestamp > DATEADD('HOUR', -1, CURRENT_TIMESTAMP)
GROUP BY ip_address
HAVING COUNT(*) >= 3;
```

#### Ver Usuários com Email
```sql
SELECT id, username, email, enabled, created_at 
FROM usuarios 
WHERE email IS NOT NULL
ORDER BY created_at DESC;
```

---

## 📧 VERIFICAR EMAILS NO MAILHOG

### Abrir Interface
```
http://localhost:8025
```

### Emails Esperados

#### 1. Password Reset Request
- **Assunto:** Redefinir sua senha - Neuroefficiency
- **De:** noreply@neuroefficiency.local
- **Formato:** HTML + Texto
- **Conteúdo:**
  - Saudação
  - Botão "Redefinir Senha"
  - Link com token
  - Aviso de expiração (30min)
  - Link de ajuda

#### 2. Password Changed Confirmation
- **Assunto:** Senha alterada com sucesso - Neuroefficiency
- **De:** noreply@neuroefficiency.local
- **Formato:** HTML + Texto
- **Conteúdo:**
  - Confirmação da alteração
  - Data/hora
  - Aviso de segurança
  - Link de ajuda

---

## 🎯 MATRIZ DE TESTES

| # | Teste | Status | Tempo |
|---|-------|--------|-------|
| 1 | Criar usuário com email | ✅ | 2s |
| 2 | Solicitar reset | ✅ | 2s |
| 3 | Validar token | ✅ | 1s |
| 4 | Confirmar reset | ✅ | 2s |
| 5 | Login nova senha | ✅ | 2s |
| 6 | Senha antiga bloqueada | ✅ | 1s |
| 7 | Token invalidado | ✅ | 1s |
| 8 | Rate limiting | ✅ | 5s |
| 9 | Anti-enumeração | ✅ | 2s |
| 10 | Internacionalização | ✅ | 2s |
| **Total** | **10 cenários** | **✅ 100%** | **~20s** |

---

## 🐛 TROUBLESHOOTING

### Email não chega no MailHog
**Verificar:**
1. MailHog rodando? `http://localhost:8025`
2. Backend usando profile `dev`?
3. Porta 1025 disponível?

**Solução:**
```bash
# Reiniciar MailHog
docker restart mailhog
```

### Rate Limiting bloqueia testes
**Problema:** 429 Too Many Requests

**Solução 1:** Aguardar 1 hora

**Solução 2:** Reiniciar backend (limpa banco H2)
```bash
# Parar backend (Ctrl+C)
# Iniciar novamente
.\mvnw.cmd spring-boot:run
```

**Solução 3:** Usar `test-fresh.ps1` (cria usuário com timestamp único)

### Token não é extraído do email
**Problema:** Script `test-complete-auto.ps1` falha na extração

**Solução Manual:**
1. Abrir MailHog: http://localhost:8025
2. Clicar no email
3. Copiar token (64 caracteres hexadecimais)
4. Usar comandos manuais dos cenários acima

---

## 📚 COLLECTION POSTMAN

### Importar Collection
```
Arquivo: Neuroefficiency_Auth.postman_collection.json
Versão: 2.0 (9 endpoints)
```

### Executar Todos os Testes
1. Importar collection no Postman
2. Executar a pasta inteira
3. Ver resultados dos testes automatizados

**Documentação:** `DOCS/GUIA_POSTMAN.md`

---

## 🔄 TESTES AUTOMATIZADOS (Futuro)

### JUnit 5 + MockMvc (Planejado)
```java
@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetIntegrationTest {
    // Testes unitários e de integração
}
```

### Cobertura Esperada
- Unit Tests: Services
- Integration Tests: Controllers
- E2E Tests: Fluxo completo

**Status:** ⏳ Planejado para Fase 3

---

## 💡 DICAS

### Para Testes Rápidos
- Use `test-complete-auto.ps1` - 100% automatizado
- Mantenha MailHog sempre rodando
- Reinicie backend para limpar rate limiting

### Para Debug
- Use H2 Console para verificar dados
- Veja logs do backend (console)
- MailHog mostra todos os emails enviados

### Boas Práticas
- Sempre teste em ordem (cenários 1-10)
- Reinicie ambiente entre testes completos
- Documente bugs encontrados

---

**Testes Completos!** ✅ Sistema 100% funcional!

