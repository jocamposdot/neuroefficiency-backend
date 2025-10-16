# Script de Teste da API de Autenticação - Neuroefficiency
# Data: 2025-10-11

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "Testando API de Autenticação" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8081"

# Teste 1: Health Check
Write-Host "1. Testando Health Check..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/health" -Method Get
    Write-Host "✓ Health Check: OK" -ForegroundColor Green
    Write-Host "  Status: $($response.status)" -ForegroundColor Gray
    Write-Host "  Service: $($response.service)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Health Check: FALHOU" -ForegroundColor Red
    Write-Host "  Erro: $_" -ForegroundColor Red
    Write-Host ""
}

# Teste 2: Registro de Usuário
Write-Host "2. Testando Registro de Usuário..." -ForegroundColor Yellow
$registerBody = @{
    username = "testuser"
    password = "Test@1234"
    confirmPassword = "Test@1234"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
    Write-Host "✓ Registro: OK" -ForegroundColor Green
    Write-Host "  Mensagem: $($response.message)" -ForegroundColor Gray
    Write-Host "  Usuário ID: $($response.user.id)" -ForegroundColor Gray
    Write-Host "  Username: $($response.user.username)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Registro: FALHOU" -ForegroundColor Red
    Write-Host "  Erro: $_" -ForegroundColor Red
    Write-Host ""
}

# Teste 3: Login
Write-Host "3. Testando Login..." -ForegroundColor Yellow
$loginBody = @{
    username = "testuser"
    password = "Test@1234"
} | ConvertTo-Json

try {
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -WebSession $session
    Write-Host "✓ Login: OK" -ForegroundColor Green
    Write-Host "  Mensagem: $($response.message)" -ForegroundColor Gray
    Write-Host "  Usuário: $($response.user.username)" -ForegroundColor Gray
    Write-Host "  Session ID: $($response.sessionId)" -ForegroundColor Gray
    Write-Host ""
    
    # Teste 4: Obter dados do usuário autenticado
    Write-Host "4. Testando endpoint /me (usuário autenticado)..." -ForegroundColor Yellow
    try {
        $meResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/me" -Method Get -WebSession $session
        Write-Host "✓ Endpoint /me: OK" -ForegroundColor Green
        Write-Host "  Username: $($meResponse.username)" -ForegroundColor Gray
        Write-Host "  ID: $($meResponse.id)" -ForegroundColor Gray
        Write-Host "  Enabled: $($meResponse.enabled)" -ForegroundColor Gray
        Write-Host ""
    } catch {
        Write-Host "✗ Endpoint /me: FALHOU" -ForegroundColor Red
        Write-Host "  Erro: $_" -ForegroundColor Red
        Write-Host ""
    }
    
    # Teste 5: Logout
    Write-Host "5. Testando Logout..." -ForegroundColor Yellow
    try {
        $logoutResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/logout" -Method Post -WebSession $session
        Write-Host "✓ Logout: OK" -ForegroundColor Green
        Write-Host "  Mensagem: $($logoutResponse.message)" -ForegroundColor Gray
        Write-Host ""
    } catch {
        Write-Host "✗ Logout: FALHOU" -ForegroundColor Red
        Write-Host "  Erro: $_" -ForegroundColor Red
        Write-Host ""
    }
    
} catch {
    Write-Host "✗ Login: FALHOU" -ForegroundColor Red
    Write-Host "  Erro: $_" -ForegroundColor Red
    Write-Host ""
}

# Teste 6: Tentar registro com username duplicado
Write-Host "6. Testando registro com username duplicado (deve falhar)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
    Write-Host "✗ Teste falhou: deveria ter retornado erro" -ForegroundColor Red
    Write-Host ""
} catch {
    if ($_.Exception.Response.StatusCode -eq 409) {
        Write-Host "✓ Validação de username duplicado: OK (409 Conflict)" -ForegroundColor Green
        Write-Host ""
    } else {
        Write-Host "✗ Erro inesperado: $_" -ForegroundColor Red
        Write-Host ""
    }
}

# Teste 7: Tentar login com credenciais inválidas
Write-Host "7. Testando login com senha inválida (deve falhar)..." -ForegroundColor Yellow
$invalidLoginBody = @{
    username = "testuser"
    password = "SenhaErrada123!"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $invalidLoginBody -ContentType "application/json"
    Write-Host "✗ Teste falhou: deveria ter retornado erro" -ForegroundColor Red
    Write-Host ""
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✓ Validação de credenciais inválidas: OK (401 Unauthorized)" -ForegroundColor Green
        Write-Host ""
    } else {
        Write-Host "✗ Erro inesperado: $_" -ForegroundColor Red
        Write-Host ""
    }
}

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "Testes Concluídos!" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

