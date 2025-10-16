# ============================================================================
# Extrair Token do Email no MailHog
# ============================================================================

$mailhogUrl = "http://localhost:8025"

Write-Host "`nBuscando token no email..." -ForegroundColor Yellow

try {
    $messages = Invoke-RestMethod -Uri "$mailhogUrl/api/v2/messages" -Method GET
    
    if ($messages.total -gt 0) {
        $lastEmail = $messages.items[0]
        
        Write-Host "`nEmail encontrado:" -ForegroundColor Green
        Write-Host "  De: $($lastEmail.From.Mailbox)@$($lastEmail.From.Domain)"
        Write-Host "  Para: $($lastEmail.To[0].Mailbox)@$($lastEmail.To[0].Domain)"
        Write-Host "  Assunto: $($lastEmail.Content.Headers.Subject[0])"
        
        # Buscar no HTML
        $html = $lastEmail.Content.Body
        
        # Tentar varios padroes de regex
        $patterns = @(
            "token=([a-f0-9]{64})",
            "reset-password\?token=3D([a-f0-9]{64})",
            "([a-f0-9]{64})"
        )
        
        $tokenFound = $false
        foreach ($pattern in $patterns) {
            if ($html -match $pattern) {
                $token = $matches[1]
                Write-Host "`nTOKEN ENCONTRADO:" -ForegroundColor Green
                Write-Host "$token" -ForegroundColor Yellow
                
                # Salvar em arquivo
                $token | Out-File -FilePath "token.txt" -Encoding UTF8 -NoNewline
                Write-Host "`nToken salvo em: token.txt" -ForegroundColor Cyan
                
                # Criar comando completo para teste
                Write-Host "`n--- COMANDOS PARA TESTAR ---" -ForegroundColor Cyan
                Write-Host "`n1. Validar token:"
                Write-Host "Invoke-RestMethod -Uri 'http://localhost:8082/api/auth/password-reset/validate-token/$token' -Method GET" -ForegroundColor Yellow
                
                Write-Host "`n2. Confirmar reset:"
                Write-Host '$confirmData = @{' -ForegroundColor Yellow
                Write-Host "    token = `"$token`"" -ForegroundColor Yellow
                Write-Host '    newPassword = "NewPass@1234"' -ForegroundColor Yellow
                Write-Host '    confirmPassword = "NewPass@1234"' -ForegroundColor Yellow
                Write-Host '} | ConvertTo-Json' -ForegroundColor Yellow
                Write-Host 'Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/confirm" -Method POST -ContentType "application/json" -Headers @{"Accept-Language" = "pt-BR"} -Body $confirmData' -ForegroundColor Yellow
                
                $tokenFound = $true
                break
            }
        }
        
        if (-not $tokenFound) {
            Write-Host "`nNao foi possivel extrair o token automaticamente" -ForegroundColor Yellow
            Write-Host "Abra o MailHog manualmente: http://localhost:8025" -ForegroundColor Cyan
            Write-Host "Copie o token do email e cole aqui" -ForegroundColor Cyan
        }
        
    } else {
        Write-Host "[ERRO] Nenhum email encontrado" -ForegroundColor Red
    }
} catch {
    Write-Host "[ERRO] $($_.Exception.Message)" -ForegroundColor Red
}

