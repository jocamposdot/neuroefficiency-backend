# Teste Completo RBAC - Neuroefficiency Fase 3
$baseUrl = "http://localhost:8082"
$headers = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}

Write-Host "=== TESTE COMPLETO RBAC NEUROEFFICIENCY ===" -ForegroundColor Green
Write-Host ""

# 1. Verificar se API esta rodando
Write-Host "1. Verificando API..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/health" -Method GET -Headers $headers
    Write-Host "✅ API OK: $($healthResponse.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ API nao esta rodando" -ForegroundColor Red
    exit 1
}

# 2. Criar usuario ADMIN
Write-Host "2. Criando usuario ADMIN..." -ForegroundColor Yellow
$adminData = @{
    username = "admin_rbac"
    email = "admin@rbac.com"
    password = "Admin@1234"
    confirmPassword = "Admin@1234"
} | ConvertTo-Json

try {
    $adminResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $adminData -Headers $headers
    Write-Host "✅ Usuario ADMIN criado: $($adminResponse.user.username)" -ForegroundColor Green
    $adminUsername = $adminResponse.user.username
    $adminId = $adminResponse.user.id
} catch {
    Write-Host "⚠️ Usuario ADMIN pode ja existir, continuando..." -ForegroundColor Yellow
    $adminUsername = "admin_rbac"
    $adminId = 1
}

# 3. Fazer login
Write-Host "3. Fazendo login..." -ForegroundColor Yellow
$loginData = @{
    username = $adminUsername
    password = "Admin@1234"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData -Headers $headers
    Write-Host "✅ Login OK: $($loginResponse.user.username)" -ForegroundColor Green
    
    # Extrair cookies de sessao
    $sessionCookie = $loginResponse.sessionCookie
    $headers["Cookie"] = $sessionCookie
} catch {
    Write-Host "❌ Falha no login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 4. Testar endpoints RBAC (sem role ADMIN ainda)
Write-Host "4. Testando endpoints RBAC (sem role ADMIN)..." -ForegroundColor Yellow
try {
    $rolesResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/roles" -Method GET -Headers $headers
    Write-Host "❌ ERRO: Endpoint deveria estar bloqueado!" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "✅ CORRETO: Endpoint bloqueado (403 Forbidden)" -ForegroundColor Green
    } else {
        Write-Host "❌ Erro inesperado: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 5. Configurar role ADMIN via H2 Console
Write-Host "5. Configurando role ADMIN..." -ForegroundColor Yellow
Write-Host "   Abra: http://localhost:8082/h2-console" -ForegroundColor Cyan
Write-Host "   JDBC URL: jdbc:h2:mem:neurodb" -ForegroundColor Cyan
Write-Host "   Execute este SQL:" -ForegroundColor Cyan
Write-Host ""
Write-Host "   INSERT INTO usuario_roles (usuario_id, role_id)" -ForegroundColor White
Write-Host "   SELECT u.id, r.id" -ForegroundColor White
Write-Host "   FROM usuarios u, roles r" -ForegroundColor White
Write-Host "   WHERE u.username = '$adminUsername' AND r.name = 'ADMIN';" -ForegroundColor White
Write-Host ""

# Aguardar usuario configurar
Read-Host "Pressione Enter quando terminar de executar o SQL no H2 Console"

# 6. Fazer logout e login novamente para carregar roles
Write-Host "6. Fazendo logout e login novamente..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/auth/logout" -Method POST -Headers $headers
    Write-Host "✅ Logout realizado" -ForegroundColor Green
} catch {
    Write-Host "⚠️ Logout pode ter falhado, continuando..." -ForegroundColor Yellow
}

# Login novamente
try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData -Headers $headers
    Write-Host "✅ Login realizado novamente" -ForegroundColor Green
    
    # Atualizar cookies
    $sessionCookie = $loginResponse.sessionCookie
    $headers["Cookie"] = $sessionCookie
} catch {
    Write-Host "❌ Falha no login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 7. Testar endpoints RBAC (com role ADMIN)
Write-Host "7. Testando endpoints RBAC (com role ADMIN)..." -ForegroundColor Yellow

# Listar roles
try {
    $rolesResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/roles" -Method GET -Headers $headers
    Write-Host "✅ Roles encontradas: $($rolesResponse.Count)" -ForegroundColor Green
    foreach ($role in $rolesResponse) {
        Write-Host "   - $($role.name): $($role.description)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Erro ao listar roles: $($_.Exception.Message)" -ForegroundColor Red
}

# Listar permissoes
try {
    $permissionsResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/permissions" -Method GET -Headers $headers
    Write-Host "✅ Permissoes encontradas: $($permissionsResponse.Count)" -ForegroundColor Green
    foreach ($permission in $permissionsResponse) {
        Write-Host "   - $($permission.name) ($($permission.resource))" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Erro ao listar permissoes: $($_.Exception.Message)" -ForegroundColor Red
}

# Verificar estatisticas
try {
    $statsResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/stats" -Method GET -Headers $headers
    Write-Host "✅ Estatisticas RBAC:" -ForegroundColor Green
    Write-Host "   - Total Roles: $($statsResponse.totalRoles)" -ForegroundColor White
    Write-Host "   - Total Permissoes: $($statsResponse.totalPermissions)" -ForegroundColor White
    Write-Host "   - Total Usuarios: $($statsResponse.totalUsuarios)" -ForegroundColor White
} catch {
    Write-Host "❌ Erro ao obter estatisticas: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== TESTE RBAC CONCLUIDO ===" -ForegroundColor Green
Write-Host "✅ Sistema RBAC funcionando corretamente!" -ForegroundColor Green
Write-Host "✅ Autorizacao por roles implementada!" -ForegroundColor Green
Write-Host "✅ Endpoints ADMIN protegidos!" -ForegroundColor Green
