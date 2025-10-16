# 🧪 Guia de Teste dos Endpoints RBAC

## 📋 **PRÉ-REQUISITOS**
- ✅ API rodando em http://localhost:8082
- ✅ Migration V5 aplicada (tabelas RBAC criadas)
- ✅ SecurityConfig atualizado com autorização

---

## 🚀 **PASSO A PASSO PARA TESTAR**

### **PASSO 1: Verificar se API está rodando**
```powershell
# Execute no PowerShell:
Invoke-RestMethod -Uri "http://localhost:8082/api/auth/health" -Method GET
```
**Resultado esperado:** `{"service":"Authentication Service","version":"1.0","status":"UP"}`

### **PASSO 2: Criar usuário ADMIN (se não existir)**
```powershell
# Execute no PowerShell:
$adminData = @{
    username = "admin_test"
    email = "admin@test.com"
    password = "Admin@1234"
    confirmPassword = "Admin@1234"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/auth/register" -Method POST -Body $adminData -Headers @{"Content-Type"="application/json"}
```
**Resultado esperado:** Usuário criado com sucesso

### **PASSO 3: Fazer login**
```powershell
# Execute no PowerShell:
$loginData = @{
    username = "admin_test"
    password = "Admin@1234"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/login" -Method POST -Body $loginData -Headers @{"Content-Type"="application/json"}
$sessionCookie = $loginResponse.sessionCookie
```
**Resultado esperado:** Login realizado com sucesso

### **PASSO 4: Configurar role ADMIN no H2 Console**

1. **Acesse:** http://localhost:8082/h2-console
2. **JDBC URL:** `jdbc:h2:mem:neurodb`
3. **Username:** `sa`
4. **Password:** (deixe vazio)
5. **Execute este SQL:**
```sql
INSERT INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
FROM usuarios u, roles r
WHERE u.username = 'admin_test' AND r.name = 'ADMIN';
```

### **PASSO 5: Fazer logout e login novamente**
```powershell
# Logout
Invoke-RestMethod -Uri "http://localhost:8082/api/auth/logout" -Method POST -Headers @{"Cookie"=$sessionCookie}

# Login novamente
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/login" -Method POST -Body $loginData -Headers @{"Content-Type"="application/json"}
$sessionCookie = $loginResponse.sessionCookie
```

### **PASSO 6: Testar endpoints RBAC**

#### **6.1 GET /api/admin/rbac/roles**
```powershell
$headers = @{
    "Content-Type" = "application/json"
    "Cookie" = $sessionCookie
}

$rolesResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/admin/rbac/roles" -Method GET -Headers $headers
$rolesResponse | ConvertTo-Json -Depth 3
```
**Resultado esperado:** Lista de roles (ADMIN, CLINICO)

#### **6.2 GET /api/admin/rbac/permissions**
```powershell
$permissionsResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/admin/rbac/permissions" -Method GET -Headers $headers
$permissionsResponse | ConvertTo-Json -Depth 3
```
**Resultado esperado:** Lista de permissões (12 permissões)

#### **6.3 GET /api/admin/rbac/stats**
```powershell
$statsResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/admin/rbac/stats" -Method GET -Headers $headers
$statsResponse | ConvertTo-Json -Depth 3
```
**Resultado esperado:** Estatísticas RBAC (contadores)

---

## 🎯 **RESULTADOS ESPERADOS**

### **✅ Sucesso (com role ADMIN):**
- **Status Code:** 200 OK
- **Response:** JSON com dados das roles/permissões/estatísticas

### **❌ Falha (sem role ADMIN):**
- **Status Code:** 403 Forbidden
- **Response:** Erro de autorização

---

## 🚀 **SCRIPT AUTOMATIZADO**

Para facilitar, execute:
```powershell
powershell -ExecutionPolicy Bypass -File test-endpoints-final.ps1
```

**Nota:** Execute o SQL no H2 Console antes de rodar o script.

---

## 🔍 **TROUBLESHOOTING**

### **Problema:** 403 Forbidden
**Solução:** Usuário não tem role ADMIN. Execute o SQL no H2 Console.

### **Problema:** 401 Unauthorized
**Solução:** Sessão expirada. Faça login novamente.

### **Problema:** 500 Internal Server Error
**Solução:** Verifique se a migration V5 foi aplicada.

---

## 📊 **ENDPOINTS DISPONÍVEIS**

| Método | Endpoint | Descrição | Autorização |
|--------|----------|-----------|-------------|
| GET | `/api/admin/rbac/roles` | Listar roles | ADMIN |
| GET | `/api/admin/rbac/permissions` | Listar permissões | ADMIN |
| GET | `/api/admin/rbac/stats` | Estatísticas RBAC | ADMIN |
| POST | `/api/admin/rbac/roles` | Criar role | ADMIN |
| POST | `/api/admin/rbac/permissions` | Criar permissão | ADMIN |
| GET | `/api/admin/rbac/users/admin` | Listar usuários ADMIN | ADMIN |
| GET | `/api/admin/rbac/users/clinico` | Listar usuários CLINICO | ADMIN |

---

## 🎉 **CONCLUSÃO**

Se todos os testes passarem, o sistema RBAC está funcionando perfeitamente! 🚀
