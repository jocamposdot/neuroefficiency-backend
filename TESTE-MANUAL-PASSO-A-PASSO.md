# 🧪 TESTE MANUAL PASSO A PASSO - Recuperação de Senha

## ✅ PRÉ-REQUISITOS

- [x] Backend rodando em `http://localhost:8082`
- [x] MailHog rodando em `http://localhost:8025`

---

## 📋 PASSO 1: Criar Usuário

```powershell
$registerData = @{
    username = "manualtest"
    email = "manualtest@example.com"
    password = "Test@1234"
    confirmPassword = "Test@1234"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/register" `
    -Method POST -ContentType "application/json" -Body $registerData
```

**Resultado esperado:**
```json
{
  "message": "Usuário registrado com sucesso",
  "user": {
    "username": "manualtest",
    "email": "manualtest@example.com"
  }
}
```

---

## 📋 PASSO 2: Solicitar Reset de Senha

```powershell
$resetData = @{ email = "manualtest@example.com" } | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/request" `
    -Method POST -ContentType "application/json" `
    -Headers @{"Accept-Language" = "pt-BR"} -Body $resetData
```

**Resultado esperado:**
```json
{
  "success": true,
  "message": "Se o email existir, você receberá instruções para redefinir sua senha."
}
```

---

## 📋 PASSO 3: Buscar Token no MailHog

1. Abra o navegador: http://localhost:8025
2. Você verá um email com o assunto "Redefinir sua senha - Neuroefficiency"
3. Clique no email para abrir
4. Procure o link que começa com: `http://localhost:5173/#/reset-password?token=...`
5. Copie APENAS o token (64 caracteres hexadecimais após `token=`)

**Exemplo de token:**
```
a1b2c3d4e5f6...{64 caracteres}...xyz
```

6. Salve o token copiado em uma variável no PowerShell:

```powershell
$token = "324c792ae9514ff192f38a937837948abc85a26af89a419eaf308296dee50f49"
```

---

## 📋 PASSO 4: Validar Token

```powershell
Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/validate-token/$token" -Method GET
```

**Resultado esperado:**
```json
{
  "success": true,
  "data": { "valid": true },
  "message": "Token válido"
}
```

---

## 📋 PASSO 5: Confirmar Reset de Senha

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

**Resultado esperado:**
```json
{
  "success": true,
  "message": "Senha redefinida com sucesso! Você já pode fazer login com sua nova senha."
}
```

---

## 📋 PASSO 6: Verificar Token Invalidado

```powershell
Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/validate-token/$token" -Method GET
```

**Resultado esperado:**
```json
{
  "success": true,
  "data": { "valid": false },
  "message": "Token inválido ou expirado"
}
```

✅ **SUCESSO**: Token foi invalidado após uso!

---

## 📋 PASSO 7: Login com Nova Senha

```powershell
$loginData = @{
    username = "manualtest"
    password = "NewPass@1234"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/login" `
    -Method POST -ContentType "application/json" -Body $loginData
```

**Resultado esperado:**
```json
{
  "message": "Login realizado com sucesso",
  "user": {
    "username": "manualtest",
    "email": "manualtest@example.com"
  }
}
```

✅ **SUCESSO**: Login funcionou com a nova senha!

---

## 📋 PASSO 8: Tentar Login com Senha Antiga

```powershell
$oldLoginData = @{
    username = "manualtest"
    password = "Test@1234"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8082/api/auth/login" `
        -Method POST -ContentType "application/json" -Body $oldLoginData
    Write-Host "[ERRO] Senha antiga ainda funciona!" -ForegroundColor Red
} catch {
    Write-Host "[OK] Senha antiga foi bloqueada!" -ForegroundColor Green
}
```

**Resultado esperado:**
```
[OK] Senha antiga foi bloqueada!
```

✅ **SUCESSO**: Senha antiga não funciona mais!

---

## 📋 PASSO 9: Verificar Email de Confirmação

1. Volte ao MailHog: http://localhost:8025
2. Você deve ver 2 emails:
   - **Email 1**: "Redefinir sua senha - Neuroefficiency" (solicitação)
   - **Email 2**: "Senha alterada com sucesso - Neuroefficiency" (confirmação)
3. Abra o segundo email e confira o conteúdo

**Conteúdo esperado do email de confirmação:**
- Título: "Sua senha foi alterada"
- Data/hora da alteração
- Aviso de segurança
- Link de ajuda

✅ **SUCESSO**: Email de confirmação enviado!

---

## 📋 PASSO 10: Testar Rate Limiting

Execute o comando de reset 4 vezes seguidas:

```powershell
# Tentativa 1
Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/request" `
    -Method POST -ContentType "application/json" `
    -Headers @{"Accept-Language" = "pt-BR"} -Body $resetData

# Tentativa 2
Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/request" `
    -Method POST -ContentType "application/json" `
    -Headers @{"Accept-Language" = "pt-BR"} -Body $resetData

# Tentativa 3
Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/request" `
    -Method POST -ContentType "application/json" `
    -Headers @{"Accept-Language" = "pt-BR"} -Body $resetData

# Tentativa 4 (deve falhar)
try {
    Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/request" `
        -Method POST -ContentType "application/json" `
        -Headers @{"Accept-Language" = "pt-BR"} -Body $resetData
    Write-Host "[ERRO] Rate limit nao funcionou!" -ForegroundColor Red
} catch {
    Write-Host "[OK] Rate limit ativado na 4ª tentativa!" -ForegroundColor Green
}
```

**Resultado esperado:**
- Primeiras 3 tentativas: Sucesso
- 4ª tentativa: Erro 429 (Too Many Requests)

✅ **SUCESSO**: Rate limiting funcionando (3 tentativas/hora)!

---

## 🎉 RESUMO DOS TESTES

| # | Teste | Status |
|---|-------|--------|
| 1 | Criar usuário | ✅ |
| 2 | Solicitar reset | ✅ |
| 3 | Receber email com token | ✅ |
| 4 | Validar token | ✅ |
| 5 | Confirmar reset | ✅ |
| 6 | Token invalidado após uso | ✅ |
| 7 | Login com nova senha | ✅ |
| 8 | Senha antiga bloqueada | ✅ |
| 9 | Email de confirmação enviado | ✅ |
| 10 | Rate limiting (3/hora) | ✅ |

---

## 🔍 VERIFICAÇÕES ADICIONAIS

### Banco de Dados H2

1. Acesse: http://localhost:8082/h2-console
2. Configurações:
   - JDBC URL: `jdbc:h2:mem:neurodb`
   - User: `sa`
   - Password: (vazio)
3. Consultas úteis:

```sql
-- Ver tokens
SELECT * FROM password_reset_tokens ORDER BY created_at DESC LIMIT 5;

-- Ver auditoria
SELECT * FROM password_reset_audit ORDER BY timestamp DESC LIMIT 10;

-- Ver usuários
SELECT id, username, email FROM usuarios ORDER BY id DESC LIMIT 5;
```

### Logs do Backend

Verifique os logs no terminal do backend para ver:
- `Token de reset gerado e enviado com sucesso`
- `Senha do usuário redefinida com sucesso`
- Eventos de auditoria

---

## 📧 MailHog - Verificações de Email

### Email 1: Reset de Senha
- **Assunto**: "Redefinir sua senha - Neuroefficiency"
- **De**: noreply@neuroefficiency.local
- **Para**: manualtest@example.com
- **Conteúdo**: Botão + link com token + aviso de expiração

### Email 2: Confirmação
- **Assunto**: "Senha alterada com sucesso - Neuroefficiency"
- **De**: noreply@neuroefficiency.local
- **Para**: manualtest@example.com
- **Conteúdo**: Confirmação + data/hora + aviso de segurança

---

## ✅ CRITÉRIOS DE SUCESSO

- [ ] Todos os 10 testes passaram
- [ ] 2 emails recebidos no MailHog
- [ ] Tokens visíveis no banco H2
- [ ] Auditoria registrada
- [ ] Rate limiting funcionando
- [ ] Senha antiga bloqueada
- [ ] Token invalidado após uso

---

**🎉 TESTE COMPLETO CONCLUÍDO COM SUCESSO!**

