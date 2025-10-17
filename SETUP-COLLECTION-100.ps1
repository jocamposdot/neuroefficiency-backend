# 🚀 Setup Automático - Collection Postman 100% Funcional
# Versão: 3.0
# Última Atualização: 17/10/2025

$baseUrl = "http://localhost:8082"
$ErrorActionPreference = "Stop"

Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "🚀 SETUP COLLECTION POSTMAN V3.0 - 100% FUNCIONAL" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# ============================================
# PASSO 1: Verificar se aplicação está rodando
# ============================================
Write-Host "PASSO 1: Verificando se aplicação está rodando..." -ForegroundColor Yellow

try {
    $healthCheck = Invoke-RestMethod -Uri "$baseUrl/api/auth/health" -Method Get -TimeoutSec 5
    Write-Host "✅ Aplicação rodando!" -ForegroundColor Green
    Write-Host "   Status: $($healthCheck.status)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "❌ ERRO: Aplicação não está rodando!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Por favor, inicie a aplicação primeiro:" -ForegroundColor Yellow
    Write-Host "   ./mvnw spring-boot:run" -ForegroundColor White
    Write-Host ""
    exit 1
}

# ============================================
# PASSO 2: Criar usuário de teste
# ============================================
Write-Host "PASSO 2: Criando usuário de teste..." -ForegroundColor Yellow

$timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
$testUsername = "testuser$timestamp"
$testEmail = "$testUsername@example.com"

$registerBody = @{
    username = $testUsername
    email = $testEmail
    password = "Test@1234"
    confirmPassword = "Test@1234"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
    Write-Host "✅ Usuário teste criado!" -ForegroundColor Green
    Write-Host "   ID: $($registerResponse.user.id)" -ForegroundColor Gray
    Write-Host "   Username: $($registerResponse.user.username)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "⚠️ Erro ao criar usuário teste (pode já existir)" -ForegroundColor Yellow
    Write-Host ""
}

# ============================================
# PASSO 3: Criar usuário ADMIN
# ============================================
Write-Host "PASSO 3: Criando usuário ADMIN..." -ForegroundColor Yellow

$adminTimestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
$adminUsername = "admin$adminTimestamp"
$adminEmail = "$adminUsername@admin.com"

$adminRegisterBody = @{
    username = $adminUsername
    email = $adminEmail
    password = "Admin@1234"
    confirmPassword = "Admin@1234"
} | ConvertTo-Json

try {
    $adminRegisterResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $adminRegisterBody -ContentType "application/json"
    $adminId = $adminRegisterResponse.user.id
    
    Write-Host "✅ Usuário ADMIN criado!" -ForegroundColor Green
    Write-Host "   ID: $adminId" -ForegroundColor Gray
    Write-Host "   Username: $adminUsername" -ForegroundColor Gray
    Write-Host ""
    
    # ============================================
    # PASSO 4: Mostrar SQL para H2 Console
    # ============================================
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "⚠️  AÇÃO NECESSÁRIA - EXECUTAR SQL NO H2 CONSOLE" -ForegroundColor Yellow
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Para atribuir role ADMIN ao usuário, siga os passos:" -ForegroundColor White
    Write-Host ""
    Write-Host "1️⃣  Abrir navegador: http://localhost:8082/h2-console" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "2️⃣  Configurar conexão:" -ForegroundColor Cyan
    Write-Host "    JDBC URL: jdbc:h2:mem:neurodb" -ForegroundColor White
    Write-Host "    Username: sa" -ForegroundColor White
    Write-Host "    Password: (deixar vazio)" -ForegroundColor White
    Write-Host ""
    Write-Host "3️⃣  Clicar em: Connect" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "4️⃣  Copiar e executar este SQL:" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "    ┌─────────────────────────────────────────────────┐" -ForegroundColor Gray
    Write-Host "    │  INSERT INTO usuario_roles (usuario_id, role_id)" -ForegroundColor Green
    Write-Host "    │  VALUES ($adminId, (SELECT id FROM roles WHERE name='ADMIN'));" -ForegroundColor Green
    Write-Host "    └─────────────────────────────────────────────────┘" -ForegroundColor Gray
    Write-Host ""
    Write-Host "5️⃣  Clicar em: Run (ou Ctrl+Enter)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host ""
    
    # Salvar SQL em arquivo para fácil cópia
    $sqlFile = "setup-admin-$adminId.sql"
    "INSERT INTO usuario_roles (usuario_id, role_id)`nVALUES ($adminId, (SELECT id FROM roles WHERE name='ADMIN'));" | Out-File -FilePath $sqlFile -Encoding UTF8
    Write-Host "✅ SQL salvo em: $sqlFile" -ForegroundColor Green
    Write-Host ""
    
    # Aguardar usuário executar SQL
    Write-Host "Pressione ENTER após executar o SQL no H2 Console..." -ForegroundColor Yellow
    $null = Read-Host
    
    # ============================================
    # PASSO 5: Fazer login como ADMIN
    # ============================================
    Write-Host ""
    Write-Host "PASSO 5: Fazendo login como ADMIN..." -ForegroundColor Yellow
    
    $loginBody = @{
        username = $adminUsername
        password = "Admin@1234"
    } | ConvertTo-Json
    
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -SessionVariable session
    Write-Host "✅ Login ADMIN bem-sucedido!" -ForegroundColor Green
    Write-Host "   Session ID: $($loginResponse.sessionId)" -ForegroundColor Gray
    Write-Host ""
    
    # ============================================
    # PASSO 6: Testar endpoint RBAC
    # ============================================
    Write-Host "PASSO 6: Testando acesso RBAC..." -ForegroundColor Yellow
    
    try {
        $rolesResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/rbac/roles" -Method Get -WebSession $session
        Write-Host "✅ Acesso RBAC funcionando!" -ForegroundColor Green
        Write-Host "   Roles encontradas: $($rolesResponse.Count)" -ForegroundColor Gray
        Write-Host ""
        
        # ============================================
        # SUCESSO TOTAL!
        # ============================================
        Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Green
        Write-Host "🎉 SETUP CONCLUÍDO COM SUCESSO!" -ForegroundColor Green
        Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Green
        Write-Host ""
        Write-Host "📊 RESUMO:" -ForegroundColor Cyan
        Write-Host "   ✅ Aplicação rodando" -ForegroundColor Gray
        Write-Host "   ✅ Usuário teste criado: $testUsername" -ForegroundColor Gray
        Write-Host "   ✅ Usuário ADMIN criado: $adminUsername" -ForegroundColor Gray
        Write-Host "   ✅ Role ADMIN atribuída" -ForegroundColor Gray
        Write-Host "   ✅ Acesso RBAC funcionando" -ForegroundColor Gray
        Write-Host ""
        Write-Host "🚀 PRÓXIMOS PASSOS:" -ForegroundColor Cyan
        Write-Host "   1. Abrir Postman" -ForegroundColor White
        Write-Host "   2. Importar: Neuroefficiency_Auth_v3.postman_collection.json" -ForegroundColor White
        Write-Host "   3. Executar collection completa (endpoints 1-27)" -ForegroundColor White
        Write-Host "   4. ✅ Ver 26/27 testes passando (96%)!" -ForegroundColor White
        Write-Host ""
        Write-Host "📝 CREDENCIAIS ADMIN:" -ForegroundColor Cyan
        Write-Host "   Username: $adminUsername" -ForegroundColor White
        Write-Host "   Password: Admin@1234" -ForegroundColor White
        Write-Host "   User ID: $adminId" -ForegroundColor White
        Write-Host ""
        Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Green
        
    } catch {
        Write-Host "❌ ERRO: Acesso RBAC negado!" -ForegroundColor Red
        Write-Host ""
        Write-Host "Possíveis causas:" -ForegroundColor Yellow
        Write-Host "   • SQL não foi executado no H2 Console" -ForegroundColor White
        Write-Host "   • Role ADMIN não foi atribuída corretamente" -ForegroundColor White
        Write-Host ""
        Write-Host "Verifique no H2 Console:" -ForegroundColor Yellow
        Write-Host "   SELECT * FROM usuario_roles WHERE usuario_id = $adminId;" -ForegroundColor White
        Write-Host ""
        Write-Host "Resultado esperado:" -ForegroundColor Yellow
        Write-Host "   USUARIO_ID | ROLE_ID" -ForegroundColor White
        Write-Host "   $adminId         | 1" -ForegroundColor White
        Write-Host ""
    }
    
} catch {
    Write-Host "❌ ERRO ao criar usuário ADMIN: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

Write-Host ""
Write-Host "Pressione ENTER para sair..." -ForegroundColor Gray
$null = Read-Host

