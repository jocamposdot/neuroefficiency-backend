# Verificar estado do token no banco via API
$token = "6354c469b00b40788196675a8c540cdbcd4e9bf315c446baa1449cb7cd32b6df"

Write-Host "Consultando H2 Console para verificar estado do token..." -ForegroundColor Yellow
Write-Host "Abra: http://localhost:8082/h2-console" -ForegroundColor Cyan
Write-Host "`nConfiguracoes:" -ForegroundColor White
Write-Host "  JDBC URL: jdbc:h2:mem:neurodb" -ForegroundColor Gray
Write-Host "  User: sa" -ForegroundColor Gray
Write-Host "  Password: (vazio)" -ForegroundColor Gray

Write-Host "`nExecute esta query:" -ForegroundColor White
Write-Host "SELECT * FROM password_reset_tokens WHERE token_hash LIKE '%6354c469%';" -ForegroundColor Yellow

Write-Host "`nOu consulte todos:" -ForegroundColor White
Write-Host "SELECT id, usuario_id, expires_at, used_at, created_at FROM password_reset_tokens ORDER BY created_at DESC LIMIT 5;" -ForegroundColor Yellow

# Tentar validar token novamente
Write-Host "`n`nValidando token pela API novamente..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/validate-token/$token" -Method GET
    Write-Host "Resultado: $($response | ConvertTo-Json -Compress)" -ForegroundColor Cyan
    
    if ($response.data.valid -eq $true) {
        Write-Host "[PROBLEMA] Token ainda esta valido (deveria estar usado)" -ForegroundColor Red
    } else {
        Write-Host "[OK] Token foi invalidado" -ForegroundColor Green
    }
} catch {
    Write-Host "[ERRO] $($_.Exception.Message)" -ForegroundColor Red
}

