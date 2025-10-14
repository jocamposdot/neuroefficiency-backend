# ============================================================================
# Teste com Usuario NOVO (evita rate limiting)
# ============================================================================

$baseUrl = "http://localhost:8082"
$mailhogUrl = "http://localhost:8025"

Write-Host "`n=== TESTE MANUAL - USUARIO NOVO ===" -ForegroundColor Cyan

# Gerar timestamp unico
$timestamp = (Get-Date).ToString("yyyyMMddHHmmss")
$testEmail = "teste$timestamp@example.com"
$testUser = "user$timestamp"

Write-Host "`nUsuario gerado:" -ForegroundColor Yellow
Write-Host "  Username: $testUser" -ForegroundColor Cyan
Write-Host "  Email: $testEmail`n" -ForegroundColor Cyan

# TESTE 1: Backend
Write-Host "TESTE 1: Backend..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/api/auth/health" -Method GET
    Write-Host "[OK] Backend UP" -ForegroundColor Green
} catch {
    Write-Host "[ERRO] Backend OFF" -ForegroundColor Red
    exit 1
}

# TESTE 2: Criar Usuario
Write-Host "`nTESTE 2: Criando usuario $testUser..." -ForegroundColor Yellow
$registerData = @{
    username = $testUser
    email = $testEmail
    password = "Test@1234"
    confirmPassword = "Test@1234"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" `
        -Method POST -ContentType "application/json" -Body $registerData
    Write-Host "[OK] Usuario criado: $testEmail" -ForegroundColor Green
} catch {
    Write-Host "[ERRO] Falha ao criar usuario" -ForegroundColor Red
    Write-Host "Detalhes: $($_.Exception.Message)" -ForegroundColor Gray
    exit 1
}

# TESTE 3: Solicitar Reset
Write-Host "`nTESTE 3: Solicitando reset para $testEmail..." -ForegroundColor Yellow
$resetData = @{ email = $testEmail } | ConvertTo-Json

try {
    $resetResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/request" `
        -Method POST -ContentType "application/json" `
        -Headers @{"Accept-Language" = "pt-BR"} -Body $resetData
    Write-Host "[OK] Reset solicitado!" -ForegroundColor Green
    Write-Host "     Mensagem: $($resetResponse.message)" -ForegroundColor Cyan
} catch {
    Write-Host "[ERRO] Falha ao solicitar reset" -ForegroundColor Red
    Write-Host "Detalhes: $($_.Exception.Message)" -ForegroundColor Gray
    Write-Host "`nPossivel causa: Rate limiting (3 tentativas/hora por IP)" -ForegroundColor Yellow
    Write-Host "Solucao: Aguarde 1 hora ou reinicie o backend" -ForegroundColor Yellow
    exit 1
}

# TESTE 4: Buscar Email
Write-Host "`nTESTE 4: Buscando email no MailHog (aguarde 3s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

try {
    $messages = Invoke-RestMethod -Uri "$mailhogUrl/api/v2/messages" -Method GET
    
    # Buscar nosso email especifico
    $ourEmail = $messages.items | Where-Object { 
        $_.To[0].Mailbox + "@" + $_.To[0].Domain -eq $testEmail 
    } | Select-Object -First 1
    
    if ($ourEmail) {
        Write-Host "[OK] Email recebido!" -ForegroundColor Green
        Write-Host "     De: $($ourEmail.From.Mailbox)@$($ourEmail.From.Domain)" -ForegroundColor Cyan
        Write-Host "     Para: $testEmail" -ForegroundColor Cyan
        Write-Host "     Assunto: $($ourEmail.Content.Headers.Subject[0])" -ForegroundColor Cyan
        
        Write-Host "`n[SUCESSO] Veja o email completo no MailHog:" -ForegroundColor Green
        Write-Host "     $mailhogUrl" -ForegroundColor Yellow
        
        Write-Host "`nPara continuar o teste:" -ForegroundColor White
        Write-Host "1. Abra o MailHog no navegador" -ForegroundColor Gray
        Write-Host "2. Clique no email" -ForegroundColor Gray
        Write-Host "3. Copie o token (64 caracteres)" -ForegroundColor Gray
        Write-Host "4. Cole na variavel:" -ForegroundColor Gray
        Write-Host "   `$token = `"SEU_TOKEN_AQUI`"" -ForegroundColor Yellow
        Write-Host "5. Execute:" -ForegroundColor Gray
        Write-Host "   Invoke-RestMethod -Uri `"$baseUrl/api/auth/password-reset/validate-token/`$token`" -Method GET" -ForegroundColor Yellow
        
    } else {
        Write-Host "[AVISO] Email ainda nao recebido" -ForegroundColor Yellow
        Write-Host "Aguarde mais alguns segundos e verifique: $mailhogUrl" -ForegroundColor Cyan
    }
} catch {
    Write-Host "[ERRO] Falha ao verificar emails" -ForegroundColor Red
}

Write-Host "`n===========================================`n" -ForegroundColor Cyan

