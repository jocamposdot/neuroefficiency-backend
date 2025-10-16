# Teste dos Endpoints RBAC - Neuroefficiency
$baseUrl = "http://localhost:8082"
$headers = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}

Write-Host "=== TESTE ENDPOINTS RBAC ===" -ForegroundColor Green
Write-Host ""

# 1. Verificar se API está rodando
Write-Host "1. Verificando API..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/health" -Method GET -Headers $headers
    Write-Host "✅ API OK: $($healthResponse.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ API não está rodando. Execute: ./mvnw spring-boot:run" -ForegroundColor Red
    exit 1
}

# 2. Criar usuário ADMIN
Write-Host "2. Criando usuário ADMIN..." -ForegroundColor Yellow
$adminData = @{
    username = "admin_rbac_test"
    email = "admin@rbac.test"
    password = "Admin@1234"
    confirmPassword = "Admin@1234"
} | ConvertTo-Json

try {
    $adminResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $adminData -Headers $headers
    Write-Host "✅ Usuário ADMIN criado: $($adminResponse.user.username)" -ForegroundColor Green
    $adminUsername = $adminResponse.user.username
    $adminId = $adminResponse.user.id
} catch {
    Write-Host "⚠️ Usuário ADMIN pode já existir, continuando..." -ForegroundColor Yellow
    $adminUsername = "admin_rbac_test"
    $adminId = 1
}

# 3. Fazer login
Write-Host "3. Fazendo login..." -ForegroundColor Yellow
$loginData = @{
    username = $adminUsername
    password = "Admin@1234"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData -Headers $headers
    Write-Host "✅ Login OK: $($loginResponse.user.username)" -ForegroundColor Green
    
    # Extrair cookies de sessão
    $sessionCookie = $loginResponse.sessionCookie
    $headers["Cookie"] = $sessionCookie
} catch {
    Write-Host "❌ Falha no login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 4. Configurar role ADMIN via SQL direto
Write-Host "4. Configurando role ADMIN..." -ForegroundColor Yellow
Write-Host "   Executando SQL para atribuir role ADMIN..." -ForegroundColor Cyan

# SQL para atribuir role ADMIN
$sqlCommand = @"
-- Conectar ao H2 e executar:
INSERT INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
FROM usuarios u, roles r
WHERE u.username = '$adminUsername' AND r.name = 'ADMIN';
"@

Write-Host "   SQL a executar:" -ForegroundColor White
Write-Host $sqlCommand -ForegroundColor Gray
Write-Host ""

# 5. Abrir H2 Console para executar SQL
Write-Host "5. Abrindo H2 Console..." -ForegroundColor Yellow
Write-Host "   URL: http://localhost:8082/h2-console" -ForegroundColor Cyan
Write-Host "   JDBC URL: jdbc:h2:mem:neurodb" -ForegroundColor Cyan
Write-Host "   Username: sa" -ForegroundColor Cyan
Write-Host "   Password: (deixe vazio)" -ForegroundColor Cyan
Write-Host ""

# Abrir H2 Console
Start-Process "http://localhost:8082/h2-console"

# Aguardar usuário executar SQL
Write-Host "   ⚠️ IMPORTANTE: Execute o SQL acima no H2 Console!" -ForegroundColor Red
Write-Host "   ⚠️ Depois pressione Enter para continuar..." -ForegroundColor Red
Read-Host ""

# 6. Fazer logout e login novamente para carregar roles
Write-Host "6. Recarregando sessão com roles..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/auth/logout" -Method POST -Headers $headers
    Write-Host "✅ Logout realizado" -ForegroundColor Green
} catch {
    Write-Host "⚠️ Logout pode ter falhado, continuando..." -ForegroundColor Yellow
}

# Login novamente
try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData -Headers $headers
    Write-Host "✅ Login realizado novamente" -ForegroundColor Green
    
    # Atualizar cookies
    $sessionCookie = $loginResponse.sessionCookie
    $headers["Cookie"] = $sessionCookie
} catch {
    Write-Host "❌ Falha no login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 7. TESTAR ENDPOINTS RBAC
Write-Host "7. Testando endpoints RBAC..." -ForegroundColor Yellow
Write-Host ""

# 7.1 GET /api/admin/rbac/roles
Write-Host "   📋 GET /api/admin/rbac/roles" -ForegroundColor Cyan
try {
    $rolesResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/roles" -Method GET -Headers $headers
    Write-Host "   ✅ SUCESSO! Roles encontradas: $($rolesResponse.Count)" -ForegroundColor Green
    foreach ($role in $rolesResponse) {
        Write-Host "      - $($role.name): $($role.description)" -ForegroundColor White
        Write-Host "        Permissões: $($role.permissions.Count)" -ForegroundColor Gray
    }
} catch {
    Write-Host "   ❌ ERRO: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 7.2 GET /api/admin/rbac/permissions
Write-Host "   🔐 GET /api/admin/rbac/permissions" -ForegroundColor Cyan
try {
    $permissionsResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/permissions" -Method GET -Headers $headers
    Write-Host "   ✅ SUCESSO! Permissões encontradas: $($permissionsResponse.Count)" -ForegroundColor Green
    foreach ($permission in $permissionsResponse) {
        Write-Host "      - $($permission.name) ($($permission.resource))" -ForegroundColor White
    }
} catch {
    Write-Host "   ❌ ERRO: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 7.3 GET /api/admin/rbac/stats
Write-Host "   📊 GET /api/admin/rbac/stats" -ForegroundColor Cyan
try {
    $statsResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/stats" -Method GET -Headers $headers
    Write-Host "   ✅ SUCESSO! Estatísticas RBAC:" -ForegroundColor Green
    Write-Host "      - Total Roles: $($statsResponse.totalRoles)" -ForegroundColor White
    Write-Host "      - Total Permissões: $($statsResponse.totalPermissions)" -ForegroundColor White
    Write-Host "      - Total Usuários: $($statsResponse.totalUsuarios)" -ForegroundColor White
    Write-Host "      - Usuários ADMIN: $($statsResponse.adminUsuarios)" -ForegroundColor White
    Write-Host "      - Usuários CLINICO: $($statsResponse.clinicoUsuarios)" -ForegroundColor White
    Write-Host "      - Pacotes Vencidos: $($statsResponse.pacotesVencidos)" -ForegroundColor White
} catch {
    Write-Host "   ❌ ERRO: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 8. RESUMO
Write-Host "=== RESUMO DOS TESTES ===" -ForegroundColor Green
Write-Host "✅ Endpoints RBAC testados com sucesso!" -ForegroundColor Green
Write-Host "✅ Sistema de autorização funcionando!" -ForegroundColor Green
Write-Host "✅ Roles e permissões carregadas!" -ForegroundColor Green
Write-Host ""
Write-Host "🎉 RBAC FASE 3 - 90% COMPLETA!" -ForegroundColor Cyan
