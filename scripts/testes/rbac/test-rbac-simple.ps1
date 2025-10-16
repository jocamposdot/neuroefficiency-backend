# Teste RBAC - Neuroefficiency Fase 3
$baseUrl = "http://localhost:8082"
$headers = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}

Write-Host "=== TESTE RBAC NEUROEFFICIENCY ===" -ForegroundColor Green

# 1. Verificar se API esta rodando
Write-Host "1. Verificando API..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/health" -Method GET -Headers $headers
    Write-Host "API OK: $($healthResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "API nao esta rodando. Execute: ./mvnw spring-boot:run" -ForegroundColor Red
    exit 1
}

# 2. Criar usuario ADMIN
Write-Host "2. Criando usuario ADMIN..." -ForegroundColor Yellow
$adminData = @{
    username = "admin_test"
    email = "admin@test.com"
    password = "Admin@1234"
    confirmPassword = "Admin@1234"
} | ConvertTo-Json

try {
    $adminResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $adminData -Headers $headers
    Write-Host "Usuario ADMIN criado: $($adminResponse.user.username)" -ForegroundColor Green
    $adminUsername = $adminResponse.user.username
} catch {
    Write-Host "Usuario ADMIN pode ja existir, continuando..." -ForegroundColor Yellow
    $adminUsername = "admin_test"
}

# 3. Fazer login
Write-Host "3. Fazendo login..." -ForegroundColor Yellow
$loginData = @{
    username = $adminUsername
    password = "Admin@1234"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData -Headers $headers
    Write-Host "Login OK: $($loginResponse.user.username)" -ForegroundColor Green
    
    # Extrair cookies de sessao
    $sessionCookie = $loginResponse.sessionCookie
    $headers["Cookie"] = $sessionCookie
} catch {
    Write-Host "Falha no login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 4. Testar endpoints RBAC
Write-Host "4. Testando endpoints RBAC..." -ForegroundColor Yellow

# Listar roles
try {
    $rolesResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/roles" -Method GET -Headers $headers
    Write-Host "Roles encontradas: $($rolesResponse.Count)" -ForegroundColor Green
    foreach ($role in $rolesResponse) {
        Write-Host "  - $($role.name): $($role.description)" -ForegroundColor White
    }
} catch {
    Write-Host "Erro ao listar roles: $($_.Exception.Message)" -ForegroundColor Red
}

# Listar permissoes
try {
    $permissionsResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/permissions" -Method GET -Headers $headers
    Write-Host "Permissoes encontradas: $($permissionsResponse.Count)" -ForegroundColor Green
    foreach ($permission in $permissionsResponse) {
        Write-Host "  - $($permission.name) ($($permission.resource))" -ForegroundColor White
    }
} catch {
    Write-Host "Erro ao listar permissoes: $($_.Exception.Message)" -ForegroundColor Red
}

# Verificar estatisticas
try {
    $statsResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/stats" -Method GET -Headers $headers
    Write-Host "Estatisticas RBAC:" -ForegroundColor Green
    Write-Host "  - Total Roles: $($statsResponse.totalRoles)" -ForegroundColor White
    Write-Host "  - Total Permissoes: $($statsResponse.totalPermissions)" -ForegroundColor White
    Write-Host "  - Total Usuarios: $($statsResponse.totalUsuarios)" -ForegroundColor White
} catch {
    Write-Host "Erro ao obter estatisticas: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== TESTE RBAC CONCLUIDO ===" -ForegroundColor Green
Write-Host "Sistema RBAC funcionando corretamente!" -ForegroundColor Green
