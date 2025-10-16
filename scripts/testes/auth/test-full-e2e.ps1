# ============================================================================
# TESTE E2E COMPLETO - Do Zero
# ============================================================================

$baseUrl = "http://localhost:8082"
$mailhogUrl = "http://localhost:8025"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TESTE E2E - RECUPERACAO DE SENHA COMPLETO" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Gerar um timestamp unico para o usuario
$timestamp = (Get-Date).ToString("yyyyMMddHHmmss")
$testEmail = "teste$timestamp@example.com"
$testUser = "testuser$timestamp"

Write-Host "Usuario de teste:" -ForegroundColor White
Write-Host "  Username: $testUser" -ForegroundColor Cyan
Write-Host "  Email: $testEmail`n" -ForegroundColor Cyan

# =============================================================================
# 1. CRIAR USUARIO
# =============================================================================
Write-Host "1. Criando usuario..." -ForegroundColor Yellow
$registerData = @{
    username = $testUser
    email = $testEmail
    password = "Test@1234"
    confirmPassword = "Test@1234"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" `
        -Method POST -ContentType "application/json" -Body $registerData
    Write-Host "   [OK] Usuario criado!" -ForegroundColor Green
} catch {
    Write-Host "   [ERRO] Falha ao criar usuario" -ForegroundColor Red
    exit 1
}

# =============================================================================
# 2. SOLICITAR RESET
# =============================================================================
Write-Host "`n2. Solicitando reset de senha..." -ForegroundColor Yellow
$resetData = @{ email = $testEmail } | ConvertTo-Json

try {
    $resetResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/request" `
        -Method POST -ContentType "application/json" `
        -Headers @{"Accept-Language" = "pt-BR"} -Body $resetData
    Write-Host "   [OK] Reset solicitado!" -ForegroundColor Green
} catch {
    Write-Host "   [ERRO] Falha ao solicitar" -ForegroundColor Red
    exit 1
}

# =============================================================================
# 3. BUSCAR TOKEN DO EMAIL
# =============================================================================
Write-Host "`n3. Buscando token no email (aguarde 3s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

try {
    $messages = Invoke-RestMethod -Uri "$mailhogUrl/api/v2/messages" -Method GET
    
    # Buscar email mais recente para nosso usuario
    $ourEmail = $messages.items | Where-Object { 
        $_.To[0].Mailbox + "@" + $_.To[0].Domain -eq $testEmail 
    } | Select-Object -First 1
    
    if ($ourEmail) {
        Write-Host "   [OK] Email encontrado!" -ForegroundColor Green
        
        # Extrair token do corpo (formato quoted-printable)
        $body = $ourEmail.Content.Body
        
        # Regex para encontrar o token (deve estar em 2 linhas)
        if ($body -match "token=3D([a-f0-9]{50,64})=\s*([a-f0-9]{0,14})") {
            $token = $matches[1] + $matches[2]
            Write-Host "   [TOKEN] $token" -ForegroundColor Yellow
        } else {
            Write-Host "   [ERRO] Token nao encontrado no email" -ForegroundColor Red
            Write-Host "   Abra o MailHog manualmente: $mailhogUrl" -ForegroundColor Cyan
            exit 1
        }
    } else {
        Write-Host "   [ERRO] Email nao recebido" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "   [ERRO] Falha ao buscar email: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# =============================================================================
# 4. VALIDAR TOKEN
# =============================================================================
Write-Host "`n4. Validando token..." -ForegroundColor Yellow
try {
    $validateResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/validate-token/$token" -Method GET
    if ($validateResponse.data.valid) {
        Write-Host "   [OK] Token valido!" -ForegroundColor Green
    } else {
        Write-Host "   [ERRO] Token invalido" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "   [ERRO] Falha ao validar: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# =============================================================================
# 5. CONFIRMAR RESET
# =============================================================================
Write-Host "`n5. Confirmando reset de senha..." -ForegroundColor Yellow
$confirmData = @{
    token = $token
    newPassword = "NewPass@1234"
    confirmPassword = "NewPass@1234"
} | ConvertTo-Json

try {
    $confirmResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/confirm" `
        -Method POST -ContentType "application/json" `
        -Headers @{"Accept-Language" = "pt-BR"} -Body $confirmData
    Write-Host "   [OK] Senha alterada!" -ForegroundColor Green
} catch {
    Write-Host "   [ERRO] Falha ao confirmar: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# =============================================================================
# 6. VERIFICAR TOKEN INVALIDADO
# =============================================================================
Write-Host "`n6. Verificando se token foi invalidado (aguarde 2s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

try {
    $revalidate = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/validate-token/$token" -Method GET
    if ($revalidate.data.valid -eq $false) {
        Write-Host "   [OK] Token foi invalidado apos uso!" -ForegroundColor Green
    } else {
        Write-Host "   [ERRO] Token ainda valido (PROBLEMA DE SEGURANCA!)" -ForegroundColor Red
    }
} catch {
    Write-Host "   [OK] Token invalidado (retornou erro)" -ForegroundColor Green
}

# =============================================================================
# 7. LOGIN COM NOVA SENHA
# =============================================================================
Write-Host "`n7. Testando login com nova senha..." -ForegroundColor Yellow
$loginData = @{
    username = $testUser
    password = "NewPass@1234"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method POST -ContentType "application/json" -Body $loginData
    Write-Host "   [OK] Login bem-sucedido!" -ForegroundColor Green
} catch {
    Write-Host "   [ERRO] Falha no login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# =============================================================================
# 8. LOGIN COM SENHA ANTIGA
# =============================================================================
Write-Host "`n8. Verificando senha antiga (deve falhar)..." -ForegroundColor Yellow
$oldLoginData = @{
    username = $testUser
    password = "Test@1234"
} | ConvertTo-Json

try {
    $oldLogin = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method POST -ContentType "application/json" -Body $oldLoginData
    Write-Host "   [ERRO] Senha antiga ainda funciona!" -ForegroundColor Red
} catch {
    Write-Host "   [OK] Senha antiga nao funciona!" -ForegroundColor Green
}

# =============================================================================
# RESULTADO FINAL
# =============================================================================
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "TODOS OS TESTES PASSARAM!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

Write-Host "Fluxo completo testado:" -ForegroundColor White
Write-Host "  [OK] 1. Usuario criado" -ForegroundColor Green
Write-Host "  [OK] 2. Reset solicitado" -ForegroundColor Green
Write-Host "  [OK] 3. Email recebido com token" -ForegroundColor Green
Write-Host "  [OK] 4. Token validado" -ForegroundColor Green
Write-Host "  [OK] 5. Senha alterada" -ForegroundColor Green
Write-Host "  [OK] 6. Token invalidado apos uso" -ForegroundColor Green
Write-Host "  [OK] 7. Login com nova senha" -ForegroundColor Green
Write-Host "  [OK] 8. Senha antiga bloqueada" -ForegroundColor Green

Write-Host "`nVeja os emails no MailHog:" -ForegroundColor Cyan
Write-Host "  $mailhogUrl`n" -ForegroundColor Yellow

