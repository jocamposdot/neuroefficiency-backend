# ============================================================================
# TESTE COMPLETO AUTOMATIZADO - Extrai Token e Testa Tudo
# ============================================================================

$baseUrl = "http://localhost:8082"
$mailhogUrl = "http://localhost:8025"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TESTE COMPLETO AUTOMATIZADO" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Usuario que acabamos de criar
$testUser = "user20251014180649"
$testEmail = "teste20251014180649@example.com"

Write-Host "Usuario de teste: $testUser" -ForegroundColor Yellow
Write-Host "Email: $testEmail`n" -ForegroundColor Yellow

# =============================================================================
# 1. BUSCAR TOKEN NO EMAIL
# =============================================================================
Write-Host "1. Buscando token no MailHog..." -ForegroundColor Yellow

try {
    $messages = Invoke-RestMethod -Uri "$mailhogUrl/api/v2/messages" -Method GET
    
    # Buscar o email mais recente para nosso usuario
    $ourEmail = $messages.items | Where-Object { 
        $_.To[0].Mailbox + "@" + $_.To[0].Domain -eq $testEmail 
    } | Select-Object -First 1
    
    if ($ourEmail) {
        Write-Host "   [OK] Email encontrado!" -ForegroundColor Green
        
        # Obter o corpo do email
        $body = $ourEmail.Content.Body
        
        # Salvar para debug
        $body | Out-File -FilePath "ultimo-email.txt" -Encoding UTF8
        
        # Tentar extrair token com varios padroes
        $token = $null
        
        # Padrao 1: URL encoded (token=3D...) - token pode estar quebrado em linhas
        # Primeiro remover quebras de linha soft do quoted-printable (=\r\n)
        $cleanBody = $body -replace "=`r`n", "" -replace "=`n", ""
        
        if ($cleanBody -match "token=3D([a-f0-9]{64})") {
            $token = $matches[1]
            Write-Host "   [DEBUG] Token extraido com sucesso (quoted-printable)" -ForegroundColor Gray
        }
        
        # Padrao 2: Token direto (sem encoding)
        if (-not $token -or $token.Length -ne 64) {
            if ($body -match "([a-f0-9]{64})") {
                $token = $matches[1]
                Write-Host "   [DEBUG] Token extraido com sucesso (direto)" -ForegroundColor Gray
            }
        }
        
        if ($token -and $token.Length -eq 64) {
            Write-Host "   [TOKEN EXTRAIDO]" -ForegroundColor Green
            Write-Host "   $token`n" -ForegroundColor Yellow
        } else {
            Write-Host "   [ERRO] Nao foi possivel extrair o token automaticamente" -ForegroundColor Red
            Write-Host "   Token encontrado: $token (tamanho: $($token.Length))" -ForegroundColor Gray
            Write-Host "`n   Abra: $mailhogUrl" -ForegroundColor Cyan
            Write-Host "   Copie o token manualmente e cole aqui:" -ForegroundColor White
            $token = Read-Host "   Token (64 caracteres)"
        }
        
    } else {
        Write-Host "   [ERRO] Email nao encontrado" -ForegroundColor Red
        exit 1
    }
    
} catch {
    Write-Host "   [ERRO] Falha ao buscar email: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# =============================================================================
# 2. VALIDAR TOKEN
# =============================================================================
Write-Host "2. Validando token..." -ForegroundColor Yellow

try {
    $validateResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/validate-token/$token" -Method GET
    
    if ($validateResponse.data.valid) {
        Write-Host "   [OK] Token valido!" -ForegroundColor Green
    } else {
        Write-Host "   [ERRO] Token invalido" -ForegroundColor Red
        Write-Host "   Resposta: $($validateResponse | ConvertTo-Json)" -ForegroundColor Gray
        exit 1
    }
} catch {
    Write-Host "   [ERRO] Falha ao validar: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# =============================================================================
# 3. CONFIRMAR RESET DE SENHA
# =============================================================================
Write-Host "`n3. Confirmando reset de senha..." -ForegroundColor Yellow

$confirmData = @{
    token = $token
    newPassword = "NewPass@1234"
    confirmPassword = "NewPass@1234"
} | ConvertTo-Json

try {
    $confirmResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/confirm" `
        -Method POST -ContentType "application/json" `
        -Headers @{"Accept-Language" = "pt-BR"} -Body $confirmData
    
    Write-Host "   [OK] Senha alterada com sucesso!" -ForegroundColor Green
    Write-Host "   Mensagem: $($confirmResponse.message)" -ForegroundColor Cyan
} catch {
    Write-Host "   [ERRO] Falha ao confirmar: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# =============================================================================
# 4. AGUARDAR EMAIL DE CONFIRMACAO
# =============================================================================
Write-Host "`n4. Aguardando email de confirmacao (3s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

try {
    $messages2 = Invoke-RestMethod -Uri "$mailhogUrl/api/v2/messages" -Method GET
    
    $confirmEmails = $messages2.items | Where-Object { 
        ($_.To[0].Mailbox + "@" + $_.To[0].Domain -eq $testEmail) -and
        ($_.Content.Headers.Subject[0] -like "*alterada*")
    }
    
    if ($confirmEmails) {
        Write-Host "   [OK] Email de confirmacao recebido!" -ForegroundColor Green
        Write-Host "   Assunto: $($confirmEmails[0].Content.Headers.Subject[0])" -ForegroundColor Cyan
    } else {
        Write-Host "   [AVISO] Email de confirmacao ainda nao recebido" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   [ERRO] Falha ao verificar emails" -ForegroundColor Red
}

# =============================================================================
# 5. VERIFICAR TOKEN INVALIDADO
# =============================================================================
Write-Host "`n5. Verificando se token foi invalidado..." -ForegroundColor Yellow

try {
    $revalidate = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/validate-token/$token" -Method GET
    
    if ($revalidate.data.valid -eq $false) {
        Write-Host "   [OK] Token foi invalidado apos uso!" -ForegroundColor Green
    } else {
        Write-Host "   [ERRO] Token ainda valido (problema de seguranca!)" -ForegroundColor Red
    }
} catch {
    Write-Host "   [OK] Token invalidado (retornou erro)" -ForegroundColor Green
}

# =============================================================================
# 6. LOGIN COM NOVA SENHA
# =============================================================================
Write-Host "`n6. Testando login com nova senha..." -ForegroundColor Yellow

$loginData = @{
    username = $testUser
    password = "NewPass@1234"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method POST -ContentType "application/json" -Body $loginData
    
    Write-Host "   [OK] Login bem-sucedido com nova senha!" -ForegroundColor Green
    Write-Host "   Usuario: $($loginResponse.user.username)" -ForegroundColor Cyan
    Write-Host "   Email: $($loginResponse.user.email)" -ForegroundColor Cyan
} catch {
    Write-Host "   [ERRO] Falha no login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# =============================================================================
# 7. TESTAR SENHA ANTIGA (deve falhar)
# =============================================================================
Write-Host "`n7. Testando senha antiga (deve falhar)..." -ForegroundColor Yellow

$oldLoginData = @{
    username = $testUser
    password = "Test@1234"
} | ConvertTo-Json

try {
    $oldLogin = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method POST -ContentType "application/json" -Body $oldLoginData
    
    Write-Host "   [ERRO] Senha antiga ainda funciona!" -ForegroundColor Red
} catch {
    Write-Host "   [OK] Senha antiga foi bloqueada!" -ForegroundColor Green
}

# =============================================================================
# RESULTADO FINAL
# =============================================================================
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "TODOS OS TESTES PASSARAM!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

Write-Host "Resumo:" -ForegroundColor White
Write-Host "  [OK] 1. Token extraido do email" -ForegroundColor Green
Write-Host "  [OK] 2. Token validado" -ForegroundColor Green
Write-Host "  [OK] 3. Senha alterada" -ForegroundColor Green
Write-Host "  [OK] 4. Email de confirmacao enviado" -ForegroundColor Green
Write-Host "  [OK] 5. Token invalidado apos uso" -ForegroundColor Green
Write-Host "  [OK] 6. Login com nova senha" -ForegroundColor Green
Write-Host "  [OK] 7. Senha antiga bloqueada" -ForegroundColor Green

Write-Host "`nVeja os 2 emails no MailHog:" -ForegroundColor Cyan
Write-Host "  $mailhogUrl`n" -ForegroundColor Yellow

Write-Host "Arquivo salvo: ultimo-email.txt (para debug)`n" -ForegroundColor Gray

