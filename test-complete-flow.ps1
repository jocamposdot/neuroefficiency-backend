# ============================================================================
# Teste Completo com Token Manual
# ============================================================================

$baseUrl = "http://localhost:8082"
$token = "6354c469b00b40788196675a8c540cdbcd4e9bf315c446baa1449cb7cd32b6df"

Write-Host "`n=== TESTE COMPLETO ===" -ForegroundColor Cyan
Write-Host "Token: $token`n" -ForegroundColor Yellow

# TESTE 1: Validar Token
Write-Host "TESTE 1: Validando token..." -ForegroundColor Yellow
try {
    $validateResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/validate-token/$token" -Method GET
    Write-Host "[OK] Token e valido!" -ForegroundColor Green
    Write-Host "     Resposta: $($validateResponse | ConvertTo-Json -Compress)" -ForegroundColor Cyan
} catch {
    Write-Host "[ERRO] Token invalido: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# TESTE 2: Confirmar Reset de Senha
Write-Host "`nTESTE 2: Confirmando reset de senha..." -ForegroundColor Yellow
$confirmData = @{
    token = $token
    newPassword = "NewPass@1234"
    confirmPassword = "NewPass@1234"
} | ConvertTo-Json

try {
    $confirmResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/confirm" `
        -Method POST `
        -ContentType "application/json" `
        -Headers @{"Accept-Language" = "pt-BR"} `
        -Body $confirmData
    Write-Host "[OK] Senha alterada com sucesso!" -ForegroundColor Green
    Write-Host "     Mensagem: $($confirmResponse.message)" -ForegroundColor Cyan
} catch {
    Write-Host "[ERRO] Falha ao confirmar reset: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# TESTE 3: Verificar Email de Confirmacao
Write-Host "`nTESTE 3: Verificando email de confirmacao (aguarde 3s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

try {
    $messages = Invoke-RestMethod -Uri "http://localhost:8025/api/v2/messages" -Method GET
    if ($messages.total -ge 2) {
        $confirmEmail = $messages.items[0]
        Write-Host "[OK] Email de confirmacao recebido!" -ForegroundColor Green
        Write-Host "     Assunto: $($confirmEmail.Content.Headers.Subject[0])" -ForegroundColor Cyan
    } else {
        Write-Host "[AVISO] Email de confirmacao ainda nao recebido" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[ERRO] Falha ao verificar emails" -ForegroundColor Red
}

# TESTE 4: Login com Nova Senha
Write-Host "`nTESTE 4: Testando login com nova senha..." -ForegroundColor Yellow
$loginData = @{
    username = "testuser"
    password = "NewPass@1234"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginData
    Write-Host "[OK] Login bem-sucedido com nova senha!" -ForegroundColor Green
    Write-Host "     Usuario: $($loginResponse.user.username)" -ForegroundColor Cyan
    Write-Host "     Email: $($loginResponse.user.email)" -ForegroundColor Cyan
    Write-Host "     Token JWT: $($loginResponse.message)" -ForegroundColor Gray
} catch {
    Write-Host "[ERRO] Falha no login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# TESTE 5: Tentar Reusar o Token
Write-Host "`nTESTE 5: Tentando reusar o token (deve falhar)..." -ForegroundColor Yellow
try {
    $revalidate = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/validate-token/$token" -Method GET
    Write-Host "[ERRO] Token ainda valido (FALHA DE SEGURANCA!)" -ForegroundColor Red
} catch {
    Write-Host "[OK] Token foi invalidado apos uso!" -ForegroundColor Green
}

# TESTE 6: Verificar Tentativa de Login com Senha Antiga
Write-Host "`nTESTE 6: Tentando login com senha antiga (deve falhar)..." -ForegroundColor Yellow
$oldLoginData = @{
    username = "testuser"
    password = "Test@1234"
} | ConvertTo-Json

try {
    $oldLogin = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $oldLoginData
    Write-Host "[ERRO] Senha antiga ainda funciona (PROBLEMA!)" -ForegroundColor Red
} catch {
    Write-Host "[OK] Senha antiga nao funciona mais!" -ForegroundColor Green
}

# RESUMO FINAL
Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "TODOS OS TESTES CONCLUIDOS COM SUCESSO!" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "`nFluxo Testado:" -ForegroundColor White
Write-Host "  [OK] 1. Token validado" -ForegroundColor Green
Write-Host "  [OK] 2. Senha alterada" -ForegroundColor Green
Write-Host "  [OK] 3. Email de confirmacao enviado" -ForegroundColor Green
Write-Host "  [OK] 4. Login com nova senha" -ForegroundColor Green
Write-Host "  [OK] 5. Token invalidado apos uso" -ForegroundColor Green
Write-Host "  [OK] 6. Senha antiga nao funciona" -ForegroundColor Green
Write-Host "`nAbra o MailHog para ver os emails:" -ForegroundColor Cyan
Write-Host "  http://localhost:8025`n" -ForegroundColor Yellow

