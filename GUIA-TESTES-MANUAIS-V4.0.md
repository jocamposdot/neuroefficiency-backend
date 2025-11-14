# 🧪 GUIA DE TESTES MANUAIS - v4.0 + v3.2.0

**Versão:** 4.0 + v3.2.0  
**Data:** 14 de Novembro de 2025  
**Objetivo:** Testar manualmente todas as funcionalidades implementadas

---

## 🎯 **PRÉ-REQUISITOS**

- Java 21 instalado
- Maven 3.8+ instalado
- Postman instalado (opcional, mas recomendado)
- Terminal/PowerShell aberto

---

## 🚀 **PASSO 1: INICIAR A APLICAÇÃO**

### **Opção A: Via Maven**

```bash
./mvnw spring-boot:run
```

### **Opção B: Via PowerShell (com perfil específico)**

```powershell
# Modo DEV (email loga no console)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Modo PROD (email envia via SMTP)
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

**Aguarde a mensagem:**
```
Started NeuroefficiencyApplication in X.XXX seconds
```

**URL da aplicação:**
```
http://localhost:8082
```

---

## 🧪 **PASSO 2: TESTES COM cURL (Terminal)**

### **2.1 Health Check** ✅

```bash
curl http://localhost:8082/api/auth/health
```

**Resposta esperada:**
```json
{
  "status": "UP",
  "timestamp": "2025-11-14T..."
}
```

---

### **2.2 Setup Admin (NOVO v3.2.0)** 🆕

**⚠️ IMPORTANTE:** Este endpoint só funciona quando NÃO existe nenhum admin no sistema.

```bash
curl -X POST http://localhost:8082/api/auth/setup-admin \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"Admin@1234\",\"confirmPassword\":\"Admin@1234\",\"email\":\"admin@neuro.com\"}"
```

**Resposta esperada (201 Created):**
```json
{
  "message": "Administrador configurado com sucesso. Sistema pronto para uso.",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@neuro.com",
    "enabled": true
  },
  "token": null
}
```

**Se já existir admin (409 Conflict):**
```json
{
  "timestamp": "2025-11-14T...",
  "status": 409,
  "error": "Admin Already Exists",
  "message": "Já existe pelo menos um administrador no sistema...",
  "path": "/api/auth/setup-admin"
}
```

---

### **2.3 Login como Admin**

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"Admin@1234\"}" \
  -c cookies.txt
```

**Resposta esperada (200 OK):**
```json
{
  "message": "Login realizado com sucesso",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@neuro.com",
    "enabled": true,
    "roles": ["ADMIN"]
  }
}
```

**⚠️ IMPORTANTE:** O cookie de sessão foi salvo em `cookies.txt`

---

### **2.4 Ver Perfil (Me)**

```bash
curl http://localhost:8082/api/auth/me -b cookies.txt
```

**Resposta esperada (200 OK):**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@neuro.com",
  "enabled": true,
  "roles": ["ADMIN"]
}
```

---

## 📊 **PASSO 3: TESTES AUDIT LOGGING (FASE 4)** 🆕

### **3.1 Ver Estatísticas de Auditoria**

```bash
curl http://localhost:8082/api/admin/audit/stats -b cookies.txt
```

**Resposta esperada (200 OK):**
```json
{
  "totalLogs": 3,
  "totalUsers": 1,
  "totalEventTypes": 2,
  "logsByEventType": {
    "SETUP_ADMIN": 1,
    "LOGIN_SUCCESS": 1,
    "USER_PROFILE_ACCESSED": 1
  },
  "recentActivity": [
    {
      "id": 3,
      "eventType": "USER_PROFILE_ACCESSED",
      "username": "admin",
      "userId": 1,
      "action": "Perfil acessado",
      "description": "Usuário admin acessou seu perfil",
      "timestamp": "2025-11-14T...",
      "success": true
    }
  ]
}
```

---

### **3.2 Ver Todos os Logs**

```bash
curl "http://localhost:8082/api/admin/audit/logs?page=0&size=10" -b cookies.txt
```

**Resposta esperada (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "eventType": "SETUP_ADMIN",
      "username": "admin",
      "userId": 1,
      "action": "Admin configurado",
      "description": "Primeiro administrador criado",
      "ipAddress": "127.0.0.1",
      "timestamp": "2025-11-14T...",
      "success": true
    },
    {
      "id": 2,
      "eventType": "LOGIN_SUCCESS",
      "username": "admin",
      "userId": 1,
      "action": "Login realizado",
      "description": "Usuário admin fez login com sucesso",
      "timestamp": "2025-11-14T...",
      "success": true
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

---

### **3.3 Ver Logs de um Usuário Específico**

```bash
curl http://localhost:8082/api/admin/audit/logs/user/1 -b cookies.txt
```

**Resposta esperada (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "eventType": "SETUP_ADMIN",
      "username": "admin",
      "userId": 1,
      "action": "Admin configurado",
      "timestamp": "2025-11-14T...",
      "success": true
    }
  ],
  "totalElements": 3
}
```

---

### **3.4 Ver Logs Recentes (últimas 24h)**

```bash
curl http://localhost:8082/api/admin/audit/logs/recent -b cookies.txt
```

---

### **3.5 Buscar Logs por IP**

```bash
curl "http://localhost:8082/api/admin/audit/logs/search?keyword=127.0.0.1" -b cookies.txt
```

---

### **3.6 Ver Estatísticas de um Usuário**

```bash
curl http://localhost:8082/api/admin/audit/stats/user/1 -b cookies.txt
```

**Resposta esperada (200 OK):**
```json
{
  "userId": 1,
  "username": "admin",
  "totalActions": 3,
  "successfulActions": 3,
  "failedActions": 0,
  "lastActivity": "2025-11-14T...",
  "actionsByType": {
    "SETUP_ADMIN": 1,
    "LOGIN_SUCCESS": 1,
    "USER_PROFILE_ACCESSED": 1
  }
}
```

---

## 🔒 **PASSO 4: TESTES RBAC (FASE 3)**

### **4.1 Ver Estatísticas RBAC**

```bash
curl http://localhost:8082/api/admin/rbac/stats -b cookies.txt
```

---

### **4.2 Criar Nova Role**

```bash
curl -X POST http://localhost:8082/api/admin/rbac/roles \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d "{\"name\":\"MANAGER\",\"description\":\"Gerente do sistema\"}"
```

**Resposta esperada (201 Created):**
```json
{
  "id": 2,
  "name": "MANAGER",
  "description": "Gerente do sistema",
  "active": true,
  "createdAt": "2025-11-14T..."
}
```

---

### **4.3 Ver Logs de Auditoria da Role Criada**

```bash
curl http://localhost:8082/api/admin/audit/logs/event/ROLE_CREATED -b cookies.txt
```

**Verá o log da criação da role MANAGER!** ✅

---

## 📧 **PASSO 5: TESTAR EMAIL (v3.2.0)** 🆕

### **5.1 Modo DEV (Email no Console)**

**Configuração:** `application-dev.properties`
```properties
app.email.enabled=false
```

**Testar Recovery de Senha:**

```bash
curl -X POST http://localhost:8082/api/auth/password-reset/request \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@neuro.com\"}"
```

**Resultado esperado:**
- ✅ Resposta 200 OK
- ✅ Email **NÃO** é enviado
- ✅ Conteúdo do email aparece nos **logs do console da aplicação**

**Verifique o console da aplicação:**
```
INFO ... - Email desabilitado (app.email.enabled=false). 
          Email para admin@neuro.com com assunto 'Recuperação de Senha' logado no console.
INFO ... - Conteúdo HTML:
<!DOCTYPE html>
<html>
<body>
    <h1>Recuperação de Senha</h1>
    ...
</body>
</html>
```

---

### **5.2 Modo PROD (Email Real)**

**Configuração:** `application-prod.properties`
```properties
app.email.enabled=true
spring.mail.host=smtp.sendgrid.net
spring.mail.username=YOUR_USERNAME
spring.mail.password=YOUR_PASSWORD
```

**⚠️ Requer configuração de servidor SMTP válido**

---

## 🧪 **PASSO 6: TESTES COM POSTMAN**

### **6.1 Importar Collection**

1. Abrir Postman
2. Ir em **Import**
3. Selecionar: `Neuroefficiency_Auth_v3.postman_collection.json`

### **6.2 Executar Testes na Ordem**

**Ordem recomendada:**

1. ✅ **Health Check**
2. ✅ **Setup Admin** (v3.2.0 - NOVO)
3. ✅ **Login**
4. ✅ **Me (Get Current User)**
5. ✅ **RBAC - Stats**
6. ✅ **Audit - Stats** (Fase 4 - NOVO)
7. ✅ **Audit - All Logs** (Fase 4 - NOVO)
8. ✅ **Audit - Logs by User** (Fase 4 - NOVO)
9. ✅ **Logout**

**Vantagem do Postman:**
- ✅ Salva automaticamente cookies
- ✅ Testes automatizados executam
- ✅ Interface visual
- ✅ Histórico de requests

---

## 🔄 **PASSO 7: FLUXO COMPLETO DE TESTE**

### **Cenário: Setup e Auditoria Completa**

```bash
# 1. Setup Admin (primeira vez)
curl -X POST http://localhost:8082/api/auth/setup-admin \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"Admin@1234\",\"confirmPassword\":\"Admin@1234\",\"email\":\"admin@neuro.com\"}"

# 2. Login
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"Admin@1234\"}" \
  -c cookies.txt

# 3. Ver meu perfil
curl http://localhost:8082/api/auth/me -b cookies.txt

# 4. Criar uma role
curl -X POST http://localhost:8082/api/admin/rbac/roles \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d "{\"name\":\"DEVELOPER\",\"description\":\"Desenvolvedor\"}"

# 5. Ver estatísticas de auditoria
curl http://localhost:8082/api/admin/audit/stats -b cookies.txt

# 6. Ver logs recentes
curl http://localhost:8082/api/admin/audit/logs/recent -b cookies.txt

# 7. Ver minha atividade
curl http://localhost:8082/api/admin/audit/stats/user/1 -b cookies.txt

# 8. Logout
curl -X POST http://localhost:8082/api/auth/logout -b cookies.txt
```

---

## 📝 **VALIDAÇÕES ESPERADAS**

### **✅ Checklist de Testes**

- [ ] Aplicação inicia sem erros
- [ ] Health check retorna 200 OK
- [ ] Setup admin funciona (primeira vez)
- [ ] Setup admin retorna 409 (segunda vez)
- [ ] Login funciona com credenciais corretas
- [ ] Cookie de sessão é criado
- [ ] Endpoint /me retorna dados do usuário
- [ ] Stats de auditoria mostram eventos
- [ ] Logs de auditoria são registrados
- [ ] Criação de role é auditada
- [ ] Email em DEV loga no console
- [ ] Logout funciona e invalida sessão

---

## 🐛 **TROUBLESHOOTING**

### **Problema: 403 Forbidden**

**Causa:** Cookie de sessão não está sendo enviado

**Solução:**
```bash
# Sempre usar -b cookies.txt nos requests autenticados
curl http://localhost:8082/api/admin/audit/stats -b cookies.txt
```

---

### **Problema: Setup Admin retorna 409**

**Causa:** Já existe um admin no sistema

**Solução:** Isso é esperado! O endpoint só funciona na primeira vez.

**Para testar novamente:**
1. Parar a aplicação
2. Deletar banco H2: `rm -rf target/` (ou deletar pasta target)
3. Reiniciar aplicação
4. Tentar setup admin novamente

---

### **Problema: Email não aparece no console**

**Causa:** `app.email.enabled=true` em DEV

**Solução:**
1. Verificar `src/main/resources/application-dev.properties`
2. Confirmar: `app.email.enabled=false`
3. Reiniciar aplicação

---

## 🎯 **SCRIPTS PRONTOS (PowerShell)**

### **Script de Teste Completo**

```powershell
# Salvar como: test-complete.ps1

$BASE_URL = "http://localhost:8082"

Write-Host "🧪 INICIANDO TESTES MANUAIS" -ForegroundColor Cyan

# 1. Health Check
Write-Host "`n1. Health Check..." -ForegroundColor Yellow
curl "$BASE_URL/api/auth/health"

# 2. Setup Admin
Write-Host "`n2. Setup Admin..." -ForegroundColor Yellow
curl -X POST "$BASE_URL/api/auth/setup-admin" `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"Admin@1234","confirmPassword":"Admin@1234","email":"admin@neuro.com"}'

# 3. Login
Write-Host "`n3. Login..." -ForegroundColor Yellow
curl -X POST "$BASE_URL/api/auth/login" `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"Admin@1234"}' `
  -c cookies.txt

# 4. Me
Write-Host "`n4. Get User..." -ForegroundColor Yellow
curl "$BASE_URL/api/auth/me" -b cookies.txt

# 5. Audit Stats
Write-Host "`n5. Audit Stats..." -ForegroundColor Yellow
curl "$BASE_URL/api/admin/audit/stats" -b cookies.txt

# 6. Audit Logs
Write-Host "`n6. Audit Logs..." -ForegroundColor Yellow
curl "$BASE_URL/api/admin/audit/logs?page=0&size=5" -b cookies.txt

Write-Host "`n✅ TESTES CONCLUÍDOS!" -ForegroundColor Green
```

**Executar:**
```powershell
powershell -ExecutionPolicy Bypass -File test-complete.ps1
```

---

## 📊 **ENDPOINTS DISPONÍVEIS**

### **Autenticação (6)**
- `GET /api/auth/health`
- `POST /api/auth/register`
- `POST /api/auth/setup-admin` 🆕 v3.2.0
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`

### **Recuperação de Senha (4)**
- `POST /api/auth/password-reset/request`
- `GET /api/auth/password-reset/validate-token/{token}`
- `POST /api/auth/password-reset/confirm`
- `GET /api/auth/password-reset/health`

### **RBAC (15)**
- `GET /api/admin/rbac/roles`
- `POST /api/admin/rbac/roles`
- `GET /api/admin/rbac/permissions`
- `POST /api/admin/rbac/permissions`
- `GET /api/admin/rbac/stats`
- E mais 10 endpoints...

### **Audit Logging (7)** 🆕 Fase 4
- `GET /api/admin/audit/logs`
- `GET /api/admin/audit/logs/user/{userId}`
- `GET /api/admin/audit/logs/event/{eventType}`
- `GET /api/admin/audit/stats`
- `GET /api/admin/audit/stats/user/{userId}`
- `GET /api/admin/audit/logs/recent`
- `GET /api/admin/audit/logs/search`

**TOTAL: 36 endpoints**

---

## 🎉 **CONCLUSÃO**

Após executar todos os testes, você deve ter:

✅ **Funcionalidades v3.2.0 validadas:**
- Setup admin funcionando
- Email com fallback testado

✅ **Funcionalidades Fase 4 validadas:**
- Audit logging registrando eventos
- Estatísticas sendo geradas
- Busca e filtros funcionando

✅ **Sistema completo validado:**
- 36 endpoints funcionais
- Auditoria completa
- RBAC operacional
- Autenticação segura

---

**Documentação:** `GUIA-TESTES-MANUAIS-V4.0.md`  
**Versão:** 4.0 + v3.2.0  
**Status:** ✅ Pronto para uso

