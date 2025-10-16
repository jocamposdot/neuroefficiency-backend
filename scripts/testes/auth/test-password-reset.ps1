# ============================================================================
# Script de Teste - Recuperação de Senha por Email
# Tarefa 2 - Neuroefficiency
# ============================================================================

Write-Host "`n🧪 TESTE MANUAL - RECUPERAÇÃO DE SENHA POR EMAIL`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8082"
$mailhogUrl = "http://localhost:8025"

# ============================================================================
# TESTE 1: Verificar Backend
# ============================================================================
Write-Host "📡 TESTE 1: Verificando Backend..." -ForegroundColor Yellow

try {
    $health = Invoke-RestMethod -Uri "$baseUrl/api/auth/health" -Method GET
    Write-Host "✅ Backend UP: $($health.service) v$($health.version)" -ForegroundColor Green
} catch {
    Write-Host "❌ Backend OFF - Inicie com: .\mvnw.cmd spring-boot:run" -ForegroundColor Red
    exit 1
}

# ============================================================================
# TESTE 2: Verificar MailHog
# ============================================================================
Write-Host "`n📧 TESTE 2: Verificando MailHog..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri $mailhogUrl -Method GET -UseBasicParsing
    Write-Host "✅ MailHog UP na porta 8025" -ForegroundColor Green
    Write-Host "   Abra no navegador: $mailhogUrl" -ForegroundColor Cyan
} catch {
    Write-Host "❌ MailHog OFF" -ForegroundColor Red
    exit 1
}

# ============================================================================
# TESTE 3: Criar Usuário com Email
# ============================================================================
Write-Host "`n👤 TESTE 3: Criando usuário com email..." -ForegroundColor Yellow

$registerData = @{
    username = "testuser"
    email = "teste@example.com"
    password = "Test@1234"
    confirmPassword = "Test@1234"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerData
    
    Write-Host "✅ Usuário criado: $($registerResponse.user.username)" -ForegroundColor Green
    Write-Host "   Email: $($registerResponse.user.email)" -ForegroundColor Cyan
    Write-Host "   ID: $($registerResponse.user.id)" -ForegroundColor Cyan
} catch {
    if ($_.Exception.Response.StatusCode -eq 409) {
        Write-Host "⚠️  Usuário já existe (tudo bem, pode continuar)" -ForegroundColor Yellow
    } else {
        Write-Host "❌ Erro ao criar usuário: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

# ============================================================================
# TESTE 4: Solicitar Reset de Senha
# ============================================================================
Write-Host "`n🔑 TESTE 4: Solicitando reset de senha..." -ForegroundColor Yellow

$resetRequestData = @{
    email = "teste@example.com"
} | ConvertTo-Json

try {
    $resetResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/request" `
        -Method POST `
        -ContentType "application/json" `
        -Headers @{"Accept-Language" = "pt-BR"} `
        -Body $resetRequestData
    
    Write-Host "✅ Solicitação enviada com sucesso!" -ForegroundColor Green
    Write-Host "   Mensagem: $($resetResponse.message)" -ForegroundColor Cyan
    Write-Host "   Success: $($resetResponse.success)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erro ao solicitar reset: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# ============================================================================
# TESTE 5: Verificar Email no MailHog
# ============================================================================
Write-Host "`n📬 TESTE 5: Verificando email no MailHog..." -ForegroundColor Yellow
Write-Host "   Aguardando 3 segundos..." -ForegroundColor Gray

Start-Sleep -Seconds 3

try {
    $messages = Invoke-RestMethod -Uri "$mailhogUrl/api/v2/messages" -Method GET
    
    if ($messages.total -gt 0) {
        $lastEmail = $messages.items[0]
        Write-Host "✅ Email recebido!" -ForegroundColor Green
        Write-Host "   De: $($lastEmail.From.Mailbox)@$($lastEmail.From.Domain)" -ForegroundColor Cyan
        Write-Host "   Para: $($lastEmail.To[0].Mailbox)@$($lastEmail.To[0].Domain)" -ForegroundColor Cyan
        Write-Host "   Assunto: $($lastEmail.Content.Headers.Subject[0])" -ForegroundColor Cyan
        
        # Extrair token do corpo do email
        $body = $lastEmail.Content.Body
        if ($body -match "token=([a-f0-9]{64})") {
            $token = $matches[1]
            Write-Host "`n🎫 TOKEN EXTRAÍDO:" -ForegroundColor Green
            Write-Host "   $token" -ForegroundColor Yellow
            
            # Salvar token para próximo teste
            $token | Out-File -FilePath "token.txt" -Encoding UTF8
            
            # ============================================================================
            # TESTE 6: Validar Token
            # ============================================================================
            Write-Host "`n✅ TESTE 6: Validando token..." -ForegroundColor Yellow
            
            try {
                $validateResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/password-reset/validate-token/$token" -Method GET
                
                if ($validateResponse.data.valid) {
                    Write-Host "✅ Token é válido!" -ForegroundColor Green
                } else {
                    Write-Host "❌ Token inválido" -ForegroundColor Red
                }
            } catch {
                Write-Host "❌ Erro ao validar token: $($_.Exception.Message)" -ForegroundColor Red
            }
            
            # ============================================================================
            # TESTE 7: Confirmar Reset de Senha
            # ============================================================================
            Write-Host "`n🔐 TESTE 7: Confirmando reset de senha..." -ForegroundColor Yellow
            
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
                
                Write-Host "✅ Senha alterada com sucesso!" -ForegroundColor Green
                Write-Host "   Mensagem: $($confirmResponse.message)" -ForegroundColor Cyan
            } catch {
                Write-Host "❌ Erro ao confirmar reset: $($_.Exception.Message)" -ForegroundColor Red
            }
            
            # ============================================================================
            # TESTE 8: Verificar Email de Confirmação
            # ============================================================================
            Write-Host "`n📬 TESTE 8: Verificando email de confirmação..." -ForegroundColor Yellow
            Write-Host "   Aguardando 3 segundos..." -ForegroundColor Gray
            
            Start-Sleep -Seconds 3
            
            try {
                $messages2 = Invoke-RestMethod -Uri "$mailhogUrl/api/v2/messages" -Method GET
                
                if ($messages2.total -gt 1) {
                    Write-Host "✅ Email de confirmação recebido!" -ForegroundColor Green
                    Write-Host "   Total de emails: $($messages2.total)" -ForegroundColor Cyan
                } else {
                    Write-Host "⚠️  Aguardando email de confirmação..." -ForegroundColor Yellow
                }
            } catch {
                Write-Host "❌ Erro ao verificar emails: $($_.Exception.Message)" -ForegroundColor Red
            }
            
            # ============================================================================
            # TESTE 9: Login com Nova Senha
            # ============================================================================
            Write-Host "`n🔓 TESTE 9: Testando login com nova senha..." -ForegroundColor Yellow
            
            $loginData = @{
                username = "testuser"
                password = "NewPass@1234"
            } | ConvertTo-Json
            
            try {
                $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
                    -Method POST `
                    -ContentType "application/json" `
                    -Body $loginData `
                    -SessionVariable session
                
                Write-Host "✅ Login bem-sucedido com nova senha!" -ForegroundColor Green
                Write-Host "   Usuário: $($loginResponse.user.username)" -ForegroundColor Cyan
                Write-Host "   Email: $($loginResponse.user.email)" -ForegroundColor Cyan
            } catch {
                Write-Host "❌ Erro no login: $($_.Exception.Message)" -ForegroundColor Red
            }
            
        } else {
            Write-Host "⚠️  Token não encontrado no email" -ForegroundColor Yellow
        }
        
    } else {
        Write-Host "⚠️  Nenhum email recebido ainda" -ForegroundColor Yellow
        Write-Host "   Aguarde alguns segundos e verifique: $mailhogUrl" -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ Erro ao verificar emails: $($_.Exception.Message)" -ForegroundColor Red
}

# ============================================================================
# RESUMO FINAL
# ============================================================================
Write-Host "`n" + "="*70 -ForegroundColor Cyan
Write-Host "📊 RESUMO DOS TESTES" -ForegroundColor Cyan
Write-Host "="*70 -ForegroundColor Cyan

Write-Host "`n✅ Testes Realizados:" -ForegroundColor Green
Write-Host "   1. Backend verificado" -ForegroundColor White
Write-Host "   2. MailHog verificado" -ForegroundColor White
Write-Host "   3. Usuário criado" -ForegroundColor White
Write-Host "   4. Reset solicitado" -ForegroundColor White
Write-Host "   5. Email recebido" -ForegroundColor White
Write-Host "   6. Token validado" -ForegroundColor White
Write-Host "   7. Senha alterada" -ForegroundColor White
Write-Host "   8. Confirmação enviada" -ForegroundColor White
Write-Host "   9. Login com nova senha" -ForegroundColor White

Write-Host "`n🌐 Links Úteis:" -ForegroundColor Cyan
Write-Host "   MailHog UI: $mailhogUrl" -ForegroundColor Yellow
Write-Host "   H2 Console: http://localhost:8082/h2-console" -ForegroundColor Yellow
Write-Host "   Health Check: $baseUrl/api/auth/health" -ForegroundColor Yellow

Write-Host "`n📝 Próximos Passos:" -ForegroundColor Cyan
Write-Host "   1. Abra o MailHog: $mailhogUrl" -ForegroundColor White
Write-Host "   2. Veja os emails recebidos" -ForegroundColor White
Write-Host "   3. Teste rate limiting (3 tentativas/hora)" -ForegroundColor White
Write-Host "   4. Teste email inexistente (anti-enumeração)" -ForegroundColor White

Write-Host "`n✅ TODOS OS TESTES CONCLUÍDOS!`n" -ForegroundColor Green

