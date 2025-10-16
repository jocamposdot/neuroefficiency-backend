# Teste Simples dos Endpoints RBAC
$baseUrl = "http://localhost:8082"
$headers = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}

Write-Host "=== TESTE ENDPOINTS RBAC ===" -ForegroundColor Green

# 1. Verificar API
Write-Host "1. Verificando API..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/health" -Method GET -Headers $headers
    Write-Host "API OK: $($healthResponse.status)" -ForegroundColor Green
} catch {
    Write-Host "API nao esta rodando" -ForegroundColor Red
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
    Write-Host "Usuario ADMIN pode ja existir" -ForegroundColor Yellow
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
    
    $sessionCookie = $loginResponse.sessionCookie
    $headers["Cookie"] = $sessionCookie
} catch {
    Write-Host "Falha no login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 4. Testar endpoints RBAC (sem role ADMIN)
Write-Host "4. Testando endpoints RBAC (sem role ADMIN)..." -ForegroundColor Yellow

Write-Host "   GET /api/admin/rbac/roles" -ForegroundColor Cyan
try {
    $rolesResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/roles" -Method GET -Headers $headers
    Write-Host "   ERRO: Endpoint deveria estar bloqueado!" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "   CORRETO: Endpoint bloqueado (403 Forbidden)" -ForegroundColor Green
    } else {
        Write-Host "   Erro inesperado: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 5. Instrucoes para configurar role ADMIN
Write-Host "5. Para testar completamente:" -ForegroundColor Yellow
Write-Host "   Acesse: http://localhost:8082/h2-console" -ForegroundColor Cyan
Write-Host "   JDBC URL: jdbc:h2:mem:neurodb" -ForegroundColor Cyan
Write-Host "   Execute este SQL:" -ForegroundColor Cyan
Write-Host ""
Write-Host "   INSERT INTO usuario_roles (usuario_id, role_id)" -ForegroundColor White
Write-Host "   SELECT u.id, r.id" -ForegroundColor White
Write-Host "   FROM usuarios u, roles r" -ForegroundColor White
Write-Host "   WHERE u.username = '$adminUsername' AND r.name = 'ADMIN';" -ForegroundColor White
Write-Host ""

# Abrir H2 Console
Start-Process "http://localhost:8082/h2-console"

Write-Host "   Depois execute este script novamente para testar os endpoints" -ForegroundColor Green
