# Script para configurar usuário ADMIN via H2 Console
Write-Host "=== CONFIGURANDO USUARIO ADMIN ===" -ForegroundColor Green

# 1. Abrir H2 Console
Write-Host "1. Abrindo H2 Console..." -ForegroundColor Yellow
Write-Host "   URL: http://localhost:8082/h2-console" -ForegroundColor Cyan
Write-Host "   JDBC URL: jdbc:h2:mem:neurodb" -ForegroundColor Cyan
Write-Host "   Username: sa" -ForegroundColor Cyan
Write-Host "   Password: (deixe vazio)" -ForegroundColor Cyan
Write-Host ""

# 2. Executar SQL para atribuir role ADMIN
Write-Host "2. Execute este SQL no H2 Console:" -ForegroundColor Yellow
Write-Host ""
Write-Host "-- Atribuir role ADMIN ao usuário admin_test" -ForegroundColor White
Write-Host "INSERT INTO usuario_roles (usuario_id, role_id)" -ForegroundColor White
Write-Host "SELECT u.id, r.id" -ForegroundColor White
Write-Host "FROM usuarios u, roles r" -ForegroundColor White
Write-Host "WHERE u.username = 'admin_test' AND r.name = 'ADMIN';" -ForegroundColor White
Write-Host ""

# 3. Verificar se funcionou
Write-Host "3. Verificar se funcionou:" -ForegroundColor Yellow
Write-Host "SELECT u.username, r.name as role_name" -ForegroundColor White
Write-Host "FROM usuarios u" -ForegroundColor White
Write-Host "JOIN usuario_roles ur ON u.id = ur.usuario_id" -ForegroundColor White
Write-Host "JOIN roles r ON ur.role_id = r.id" -ForegroundColor White
Write-Host "WHERE u.username = 'admin_test';" -ForegroundColor White
Write-Host ""

Write-Host "4. Depois execute: powershell -ExecutionPolicy Bypass -File test-rbac-simple.ps1" -ForegroundColor Green
Write-Host ""

# Aguardar usuário executar
Read-Host "Pressione Enter quando terminar de executar o SQL no H2 Console"
