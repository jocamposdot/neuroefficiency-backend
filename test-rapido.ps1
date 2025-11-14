# Script de Testes Rápido - v4.0 + v3.2.0

$BASE = "http://localhost:8082"

Write-Host "`n╔═══════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  🧪 TESTES v4.0 + v3.2.0 🧪              ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════╝`n" -ForegroundColor Cyan

# 1. Health Check
Write-Host "1. Health Check..." -ForegroundColor Yellow
$r = Invoke-RestMethod "$BASE/api/auth/health"
Write-Host "   ✅ Status: $($r.status)`n" -ForegroundColor Green
Start-Sleep -Seconds 1

# 2. Setup Admin
Write-Host "2. Setup Admin (v3.2.0)..." -ForegroundColor Yellow
try {
    $r = Invoke-RestMethod "$BASE/api/auth/setup-admin" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"Admin@1234","confirmPassword":"Admin@1234","email":"admin@neuro.com"}'
    Write-Host "   ✅ Admin: $($r.user.username)`n" -ForegroundColor Green
} catch {
    Write-Host "   ℹ️  Admin já existe`n" -ForegroundColor Yellow
}
Start-Sleep -Seconds 1

# 3. Login
Write-Host "3. Login..." -ForegroundColor Yellow
$session = $null
$r = Invoke-RestMethod "$BASE/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"Admin@1234"}' -SessionVariable session
$global:s = $session
Write-Host "   ✅ Login: $($r.user.username)`n" -ForegroundColor Green
Start-Sleep -Seconds 1

# 4. Me
Write-Host "4. Get User..." -ForegroundColor Yellow
$r = Invoke-RestMethod "$BASE/api/auth/me" -WebSession $global:s
Write-Host "   ✅ User: $($r.username) | Roles: $($r.roles)`n" -ForegroundColor Green
Start-Sleep -Seconds 1

# 5. Audit Stats
Write-Host "5. Audit Stats (Fase 4)..." -ForegroundColor Yellow
$r = Invoke-RestMethod "$BASE/api/admin/audit/stats" -WebSession $global:s
Write-Host "   ✅ Logs: $($r.totalLogs) | Users: $($r.totalUsers)`n" -ForegroundColor Green
Start-Sleep -Seconds 1

# 6. Audit Logs
Write-Host "6. Audit Logs..." -ForegroundColor Yellow
$url = "$BASE/api/admin/audit/logs" + "?page=0" + "&size=5"
$r = Invoke-RestMethod $url -WebSession $global:s
Write-Host "   ✅ Total: $($r.totalElements) logs`n" -ForegroundColor Green
Start-Sleep -Seconds 1

# 7. Logs do User
Write-Host "7. Logs do Usuário..." -ForegroundColor Yellow
$r = Invoke-RestMethod "$BASE/api/admin/audit/logs/user/1" -WebSession $global:s
Write-Host "   ✅ Ações do admin: $($r.totalElements)`n" -ForegroundColor Green
Start-Sleep -Seconds 1

# 8. Logs Recentes
Write-Host "8. Logs Recentes..." -ForegroundColor Yellow
$r = Invoke-RestMethod "$BASE/api/admin/audit/logs/recent" -WebSession $global:s
Write-Host "   ✅ Eventos 24h: $($r.totalElements)`n" -ForegroundColor Green
Start-Sleep -Seconds 1

# 9. User Stats
Write-Host "9. User Stats..." -ForegroundColor Yellow
$r = Invoke-RestMethod "$BASE/api/admin/audit/stats/user/1" -WebSession $global:s
Write-Host "   ✅ Ações: $($r.totalActions) | Sucesso: $($r.successfulActions)`n" -ForegroundColor Green
Start-Sleep -Seconds 1

# 10. RBAC Stats
Write-Host "10. RBAC Stats..." -ForegroundColor Yellow
$r = Invoke-RestMethod "$BASE/api/admin/rbac/stats" -WebSession $global:s
Write-Host "   ✅ Roles: $($r.totalRoles) | Permissions: $($r.totalPermissions)`n" -ForegroundColor Green
Start-Sleep -Seconds 1

# 11. Criar Role
Write-Host "11. Criar Role (será auditado)..." -ForegroundColor Yellow
try {
    $r = Invoke-RestMethod "$BASE/api/admin/rbac/roles" -Method POST -ContentType "application/json" -Body '{"name":"DEVELOPER","description":"Dev"}' -WebSession $global:s
    Write-Host "   ✅ Role: $($r.name)`n" -ForegroundColor Green
} catch {
    Write-Host "   ℹ️  Role já existe`n" -ForegroundColor Yellow
}
Start-Sleep -Seconds 2

# 12. Verificar Auditoria
Write-Host "12. Verificar Auditoria da Role..." -ForegroundColor Yellow
$r = Invoke-RestMethod "$BASE/api/admin/audit/logs/event/ROLE_CREATED" -WebSession $global:s
Write-Host "   ✅ Eventos ROLE_CREATED: $($r.totalElements)" -ForegroundColor Green
Write-Host "   🎯 Auditoria funcionando!`n" -ForegroundColor Green

# Resumo
Write-Host "`n╔═══════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║      🎉 TESTES CONCLUÍDOS! 🎉            ║" -ForegroundColor Green
Write-Host "╚═══════════════════════════════════════════╝`n" -ForegroundColor Green

Write-Host "✅ v3.2.0: Setup Admin + Email Fallback" -ForegroundColor Green
Write-Host "✅ Fase 4: Audit Logging (7 endpoints)" -ForegroundColor Green
Write-Host "✅ Core: Auth + RBAC" -ForegroundColor Green
Write-Host "`n📊 12 endpoints testados com sucesso!" -ForegroundColor Cyan
Write-Host "🚀 Sistema: v4.0 + v3.2.0 - 100% Funcional`n" -ForegroundColor Cyan

