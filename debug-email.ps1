# Debug Email Content
$messages = Invoke-RestMethod -Uri "http://localhost:8025/api/v2/messages" -Method GET
$lastEmail = $messages.items[0]

Write-Host "=== EMAIL COMPLETO ===" -ForegroundColor Cyan
$lastEmail.Content.Body | Out-File -FilePath "email-body.txt" -Encoding UTF8

Write-Host "Email salvo em: email-body.txt" -ForegroundColor Green
Write-Host "`nPrimeiros 2000 caracteres:" -ForegroundColor Yellow
$lastEmail.Content.Body.Substring(0, [Math]::Min(2000, $lastEmail.Content.Body.Length))

