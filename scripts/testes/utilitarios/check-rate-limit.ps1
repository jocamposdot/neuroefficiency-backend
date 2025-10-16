# ============================================================================
# Verificar Rate Limiting - Consultar Auditoria
# ============================================================================

Write-Host "`n=== VERIFICACAO DE RATE LIMITING ===" -ForegroundColor Cyan

Write-Host "`nH2 Console aberto no navegador." -ForegroundColor Yellow
Write-Host "Configure:" -ForegroundColor White
Write-Host "  JDBC URL: jdbc:h2:mem:neurodb" -ForegroundColor Gray
Write-Host "  User: sa" -ForegroundColor Gray
Write-Host "  Password: (vazio)`n" -ForegroundColor Gray

Write-Host "Execute esta query para ver tentativas de reset:" -ForegroundColor White
Write-Host @"

SELECT 
    email,
    ip_address,
    event_type,
    success,
    timestamp,
    error_message
FROM password_reset_audit 
WHERE timestamp > DATEADD('HOUR', -1, CURRENT_TIMESTAMP)
ORDER BY timestamp DESC;

"@ -ForegroundColor Yellow

Write-Host "`nOu conte tentativas por IP na ultima hora:" -ForegroundColor White
Write-Host @"

SELECT 
    ip_address,
    COUNT(*) as tentativas,
    MAX(timestamp) as ultima_tentativa
FROM password_reset_audit 
WHERE event_type = 'REQUEST' 
  AND timestamp > DATEADD('HOUR', -1, CURRENT_TIMESTAMP)
GROUP BY ip_address
HAVING COUNT(*) >= 3;

"@ -ForegroundColor Yellow

Write-Host "`n===========================================`n" -ForegroundColor Cyan

Write-Host "O que fazer agora?" -ForegroundColor White
Write-Host "  1. [RAPIDO] Reiniciar o backend (limpa o banco H2)" -ForegroundColor Cyan
Write-Host "  2. [LENTO] Aguardar 1 hora" -ForegroundColor Cyan
Write-Host "`nPara reiniciar o backend:" -ForegroundColor White
Write-Host "  - Pare o processo Java (Ctrl+C no terminal do backend)" -ForegroundColor Gray
Write-Host "  - Execute: .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
Write-Host "`n"

