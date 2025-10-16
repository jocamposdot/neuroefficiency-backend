# Teste Final dos Endpoints RBAC
$baseUrl = "http://localhost:8082"
$headers = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}

Write-Host "=== TESTE FINAL ENDPOINTS RBAC ===" -ForegroundColor Green

# 1. Fazer login com usuario admin_test
Write-Host "1. Fazendo login..." -ForegroundColor Yellow
$loginData = @{
    username = "admin_test"
    password = "Admin@1234"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData -Headers $headers
    Write-Host "Login OK: $($loginResponse.user.username)" -ForegroundColor Green
    
    $sessionCookie = $loginResponse.sessionCookie
    $headers["Cookie"] = $sessionCookie
} catch {
    Write-Host "Falha no login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. Testar GET /api/admin/rbac/roles
Write-Host "2. Testando GET /api/admin/rbac/roles..." -ForegroundColor Yellow
try {
    $rolesResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/roles" -Method GET -Headers $headers
    Write-Host "SUCESSO! Roles encontradas: $($rolesResponse.Count)" -ForegroundColor Green
    foreach ($role in $rolesResponse) {
        Write-Host "  - $($role.name): $($role.description)" -ForegroundColor White
    }
} catch {
    Write-Host "ERRO: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

# 3. Testar GET /api/admin/rbac/permissions
Write-Host "3. Testando GET /api/admin/rbac/permissions..." -ForegroundColor Yellow
try {
    $permissionsResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/permissions" -Method GET -Headers $headers
    Write-Host "SUCESSO! Permissoes encontradas: $($permissionsResponse.Count)" -ForegroundColor Green
    foreach ($permission in $permissionsResponse) {
        Write-Host "  - $($permission.name) ($($permission.resource))" -ForegroundColor White
    }
} catch {
    Write-Host "ERRO: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

# 4. Testar GET /api/admin/rbac/stats
Write-Host "4. Testando GET /api/admin/rbac/stats..." -ForegroundColor Yellow
try {
    $statsResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/stats" -Method GET -Headers $headers
    Write-Host "SUCESSO! Estatisticas RBAC:" -ForegroundColor Green
    Write-Host "  - Total Roles: $($statsResponse.totalRoles)" -ForegroundColor White
    Write-Host "  - Total Permissoes: $($statsResponse.totalPermissions)" -ForegroundColor White
    Write-Host "  - Total Usuarios: $($statsResponse.totalUsuarios)" -ForegroundColor White
    Write-Host "  - Usuarios ADMIN: $($statsResponse.adminUsuarios)" -ForegroundColor White
    Write-Host "  - Usuarios CLINICO: $($statsResponse.clinicoUsuarios)" -ForegroundColor White
} catch {
    Write-Host "ERRO: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== TESTE CONCLUIDO ===" -ForegroundColor Green
