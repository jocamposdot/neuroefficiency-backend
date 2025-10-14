# ============================================================================
# Teste Simples - Recuperacao de Senha por Email
# ============================================================================

Write-Host "`n=== TESTE MANUAL - RECUPERACAO DE SENHA ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8082"
$mailhogUrl = "http://localhost:8025"

# TESTE 1: Backend
Write-Host "`nTESTE 1: Verificando Backend..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/api/auth/health" -Method GET
    Write-Host "[OK] Backend UP: $($health.service)" -ForegroundColor Green
} catch {
    Write-Host "[ERRO] Backend OFF" -ForegroundColor Red
    exit 1
}

# TESTE 2: MailHog
Write-Host "`nTESTE 2: Verificando MailHog..." -ForegroundColor Yellow
try {
    $null = Invoke-WebRequest -Uri $mailhogUrl -Method GET -UseBasicParsing
    Write-Host "[OK] MailHog UP na porta 8025" -ForegroundColor Green
    Write-Host "     Acesse: $mailhogUrl" -ForegroundColor Cyan
} catch {
    Write-Host "[ERRO] MailHog OFF" -ForegroundColor Red
    exit 1
}

# TESTE 3: Criar Usuario
Write-Host "`nTESTE 3: Criando usuario..." -ForegroundColor Yellow
$registerData = @{
    username = "testuser"
    email = "teste@example.com"
    password = "Test@1234"
    confirmPassword = "Test@1234"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" `
        -Method POST -ContentType "application/json" -Body $registerData
    Write-Host "[OK] Usuario criado: $($registerResponse.user.email)" -ForegroundColor Green
} catch {
    Write-Host "[AVISO] Usuario ja existe (ok)" -ForegroundColor Yellow
}

# TESTE 4: Solicitar Reset
Write-Host "`nTESTE 4: Solicitando reset..." -ForegroundColor Yellow
$resetData = @{ email = "teste@example.com" } | ConvertTo-Json

try {
    $resetResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/request" `
        -Method POST -ContentType "application/json" `
        -Headers @{"Accept-Language" = "pt-BR"} -Body $resetData
    Write-Host "[OK] Reset solicitado!" -ForegroundColor Green
    Write-Host "     Mensagem: $($resetResponse.message)" -ForegroundColor Cyan
} catch {
    Write-Host "[ERRO] Falha ao solicitar reset" -ForegroundColor Red
}

# TESTE 5: Verificar Email
Write-Host "`nTESTE 5: Verificando email (aguarde 3s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

try {
    $messages = Invoke-RestMethod -Uri "$mailhogUrl/api/v2/messages" -Method GET
    
    if ($messages.total -gt 0) {
        $lastEmail = $messages.items[0]
        Write-Host "[OK] Email recebido!" -ForegroundColor Green
        Write-Host "     De: $($lastEmail.From.Mailbox)@$($lastEmail.From.Domain)" -ForegroundColor Cyan
        Write-Host "     Para: $($lastEmail.To[0].Mailbox)@$($lastEmail.To[0].Domain)" -ForegroundColor Cyan
        Write-Host "     Assunto: $($lastEmail.Content.Headers.Subject[0])" -ForegroundColor Cyan
        
        # Extrair token
        $body = $lastEmail.Content.Body
        if ($body -match "token=([a-f0-9]{64})") {
            $token = $matches[1]
            Write-Host "`n[TOKEN ENCONTRADO]" -ForegroundColor Green
            Write-Host "$token" -ForegroundColor Yellow
            
            # TESTE 6: Validar Token
            Write-Host "`nTESTE 6: Validando token..." -ForegroundColor Yellow
            try {
                $validateResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/validate-token/$token" -Method GET
                if ($validateResponse.data.valid) {
                    Write-Host "[OK] Token valido!" -ForegroundColor Green
                }
            } catch {
                Write-Host "[ERRO] Token invalido" -ForegroundColor Red
            }
            
            # TESTE 7: Confirmar Reset
            Write-Host "`nTESTE 7: Confirmando reset..." -ForegroundColor Yellow
            $confirmData = @{
                token = $token
                newPassword = "NewPass@1234"
                confirmPassword = "NewPass@1234"
            } | ConvertTo-Json
            
            try {
                $confirmResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/confirm" `
                    -Method POST -ContentType "application/json" `
                    -Headers @{"Accept-Language" = "pt-BR"} -Body $confirmData
                Write-Host "[OK] Senha alterada!" -ForegroundColor Green
            } catch {
                Write-Host "[ERRO] Falha ao alterar senha" -ForegroundColor Red
            }
            
            # TESTE 8: Login com Nova Senha
            Write-Host "`nTESTE 8: Login com nova senha..." -ForegroundColor Yellow
            $loginData = @{
                username = "testuser"
                password = "NewPass@1234"
            } | ConvertTo-Json
            
            try {
                $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
                    -Method POST -ContentType "application/json" -Body $loginData
                Write-Host "[OK] Login bem-sucedido!" -ForegroundColor Green
                Write-Host "     Usuario: $($loginResponse.user.username)" -ForegroundColor Cyan
            } catch {
                Write-Host "[ERRO] Falha no login" -ForegroundColor Red
            }
        }
    } else {
        Write-Host "[AVISO] Nenhum email recebido" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[ERRO] Falha ao verificar emails" -ForegroundColor Red
}

# RESUMO
Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "RESUMO DOS TESTES" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "`nLinks Uteis:" -ForegroundColor White
Write-Host "  MailHog UI: $mailhogUrl" -ForegroundColor Yellow
Write-Host "  H2 Console: http://localhost:8082/h2-console" -ForegroundColor Yellow
Write-Host "`nTestes Concluidos!`n" -ForegroundColor Green

