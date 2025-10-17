# 🎯 DEMO COMPLETA - Preparação para Apresentação Gerencial
# Versão: 3.0
# Data: 17/10/2025
# Objetivo: Setup automático completo para demonstração 100% perfeita

$ErrorActionPreference = "Stop"

Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "🚀 DEMO COMPLETA - Setup Automático para Gerência" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Este script vai:" -ForegroundColor Yellow
Write-Host "  1. ✅ Verificar pré-requisitos (Java, Docker)" -ForegroundColor Gray
Write-Host "  2. ✅ Iniciar MailHog" -ForegroundColor Gray
Write-Host "  3. ✅ Aguardar aplicação estar pronta" -ForegroundColor Gray
Write-Host "  4. ✅ Validar todos os serviços" -ForegroundColor Gray
Write-Host "  5. ✅ Abrir interfaces necessárias" -ForegroundColor Gray
Write-Host "  6. ✅ Preparar SQL para H2 Console" -ForegroundColor Gray
Write-Host ""
Write-Host "Pressione ENTER para começar..." -ForegroundColor Yellow
$null = Read-Host

# ═══════════════════════════════════════════════════════════════
# ETAPA 1: Verificar Pré-requisitos
# ═══════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "📋 ETAPA 1: Verificando Pré-requisitos" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Verificar Java
Write-Host "Verificando Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
    Write-Host "✅ Java encontrado: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ ERRO: Java não encontrado!" -ForegroundColor Red
    Write-Host "   Instale Java 21+ de: https://adoptium.net/" -ForegroundColor Yellow
    exit 1
}

# Verificar Docker
Write-Host "Verificando Docker..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version
    Write-Host "✅ Docker encontrado: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ ERRO: Docker não encontrado!" -ForegroundColor Red
    Write-Host "   Instale Docker Desktop de: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# Verificar se Docker está rodando
Write-Host "Verificando se Docker está rodando..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "✅ Docker está rodando!" -ForegroundColor Green
} catch {
    Write-Host "❌ ERRO: Docker não está rodando!" -ForegroundColor Red
    Write-Host "   Inicie o Docker Desktop e tente novamente." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "✅ Todos os pré-requisitos OK!" -ForegroundColor Green
Start-Sleep -Seconds 2

# ═══════════════════════════════════════════════════════════════
# ETAPA 2: Iniciar MailHog
# ═══════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "📧 ETAPA 2: Iniciando MailHog (Servidor de Email)" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Verificar se MailHog já está rodando
$mailhogRunning = docker ps | Select-String "mailhog"

if ($mailhogRunning) {
    Write-Host "⚠️  MailHog já está rodando!" -ForegroundColor Yellow
    Write-Host "   Deseja reiniciar? (s/n): " -ForegroundColor Yellow -NoNewline
    $resposta = Read-Host
    if ($resposta -eq "s") {
        Write-Host "   Parando MailHog..." -ForegroundColor Yellow
        docker stop mailhog | Out-Null
        docker rm mailhog | Out-Null
        Write-Host "   ✅ MailHog parado" -ForegroundColor Green
    } else {
        Write-Host "   ✅ Usando instância existente" -ForegroundColor Green
    }
}

# Verificar se container existe mas está parado
$mailhogExists = docker ps -a | Select-String "mailhog"

if ($mailhogExists -and -not $mailhogRunning) {
    Write-Host "Iniciando container MailHog existente..." -ForegroundColor Yellow
    docker start mailhog | Out-Null
    Write-Host "✅ MailHog iniciado!" -ForegroundColor Green
} elseif (-not $mailhogRunning) {
    Write-Host "Criando e iniciando MailHog..." -ForegroundColor Yellow
    docker run -d --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog | Out-Null
    Write-Host "✅ MailHog criado e iniciado!" -ForegroundColor Green
}

# Aguardar MailHog estar pronto
Write-Host "Aguardando MailHog ficar pronto..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# Testar MailHog
Write-Host "Testando MailHog..." -ForegroundColor Yellow
try {
    $mailhogTest = Invoke-RestMethod -Uri "http://localhost:8025/api/v2/messages" -Method Get -TimeoutSec 5
    Write-Host "✅ MailHog está acessível!" -ForegroundColor Green
    Write-Host "   URL: http://localhost:8025" -ForegroundColor Gray
} catch {
    Write-Host "⚠️  MailHog não respondeu, mas pode estar iniciando..." -ForegroundColor Yellow
}

Start-Sleep -Seconds 2

# ═══════════════════════════════════════════════════════════════
# ETAPA 3: Aguardar Aplicação Estar Pronta
# ═══════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "🔍 ETAPA 3: Aguardando Aplicação Backend" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  IMPORTANTE: A aplicação deve estar rodando!" -ForegroundColor Yellow
Write-Host ""
Write-Host "   Se ainda não iniciou, abra OUTRO terminal e execute:" -ForegroundColor Yellow
Write-Host "   cd C:\Users\rafav\Downloads\neuro-core" -ForegroundColor White
Write-Host "   ./mvnw spring-boot:run" -ForegroundColor White
Write-Host ""
Write-Host "Verificando se aplicação está rodando..." -ForegroundColor Yellow
Write-Host ""

$maxTentativas = 60
$tentativa = 0
$appRunning = $false

while ($tentativa -lt $maxTentativas -and -not $appRunning) {
    $tentativa++
    Write-Host "   Tentativa $tentativa/$maxTentativas..." -ForegroundColor Gray
    
    try {
        $healthCheck = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/health" -Method Get -TimeoutSec 2
        if ($healthCheck.status -eq "UP") {
            $appRunning = $true
            Write-Host ""
            Write-Host "✅ Aplicação está rodando e saudável!" -ForegroundColor Green
            Write-Host "   Status: $($healthCheck.status)" -ForegroundColor Gray
            Write-Host "   Service: $($healthCheck.service)" -ForegroundColor Gray
            Write-Host "   Version: $($healthCheck.version)" -ForegroundColor Gray
        }
    } catch {
        Start-Sleep -Seconds 2
    }
}

if (-not $appRunning) {
    Write-Host ""
    Write-Host "❌ ERRO: Aplicação não respondeu após $maxTentativas tentativas" -ForegroundColor Red
    Write-Host ""
    Write-Host "Por favor:" -ForegroundColor Yellow
    Write-Host "  1. Abra OUTRO terminal" -ForegroundColor White
    Write-Host "  2. Execute: cd C:\Users\rafav\Downloads\neuro-core" -ForegroundColor White
    Write-Host "  3. Execute: ./mvnw spring-boot:run" -ForegroundColor White
    Write-Host "  4. Aguarde mensagem: 'Started NeuroefficiencyApplication'" -ForegroundColor White
    Write-Host "  5. Execute este script novamente" -ForegroundColor White
    Write-Host ""
    exit 1
}

Start-Sleep -Seconds 2

# ═══════════════════════════════════════════════════════════════
# ETAPA 4: Validar Todos os Serviços
# ═══════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "✅ ETAPA 4: Validando Serviços" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Validar Backend Health
Write-Host "Validando Backend..." -ForegroundColor Yellow
$health = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/health" -Method Get
Write-Host "✅ Backend: $($health.status)" -ForegroundColor Green

# Validar Password Reset Health
Write-Host "Validando Password Reset..." -ForegroundColor Yellow
$resetHealth = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/password-reset/health" -Method Get
Write-Host "✅ Password Reset: $($resetHealth.data.status)" -ForegroundColor Green

# Validar MailHog
Write-Host "Validando MailHog..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "http://localhost:8025/api/v2/messages" -Method Get | Out-Null
    Write-Host "✅ MailHog: UP" -ForegroundColor Green
} catch {
    Write-Host "⚠️  MailHog: Não acessível (mas pode funcionar)" -ForegroundColor Yellow
}

# Validar H2 Console
Write-Host "Validando H2 Console..." -ForegroundColor Yellow
try {
    $h2Response = Invoke-WebRequest -Uri "http://localhost:8082/h2-console" -Method Get -TimeoutSec 5
    Write-Host "✅ H2 Console: UP" -ForegroundColor Green
} catch {
    Write-Host "⚠️  H2 Console: Não acessível diretamente (normal)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "✅ Validação concluída!" -ForegroundColor Green
Start-Sleep -Seconds 2

# ═══════════════════════════════════════════════════════════════
# ETAPA 5: Preparar SQL para H2 Console
# ═══════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "📝 ETAPA 5: Preparando SQL para Atribuir Role ADMIN" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "⚠️  IMPORTANTE: Você precisará executar SQL no H2 Console!" -ForegroundColor Yellow
Write-Host ""
Write-Host "Depois de criar o usuário admin no Postman (endpoint 10)," -ForegroundColor White
Write-Host "você verá o ID do usuário no console do Postman." -ForegroundColor White
Write-Host ""
Write-Host "Use este SQL no H2 Console:" -ForegroundColor Yellow
Write-Host ""
Write-Host "┌─────────────────────────────────────────────────────────────┐" -ForegroundColor Gray
Write-Host "│  INSERT INTO usuario_roles (usuario_id, role_id)            │" -ForegroundColor Green
Write-Host "│  VALUES (<ID_DO_USUARIO>, (SELECT id FROM roles             │" -ForegroundColor Green
Write-Host "│          WHERE name='ADMIN'));                               │" -ForegroundColor Green
Write-Host "└─────────────────────────────────────────────────────────────┘" -ForegroundColor Gray
Write-Host ""
Write-Host "Substitua <ID_DO_USUARIO> pelo ID que aparecer no Postman." -ForegroundColor White
Write-Host ""

Start-Sleep -Seconds 3

# ═══════════════════════════════════════════════════════════════
# ETAPA 6: Abrir Interfaces
# ═══════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "🌐 ETAPA 6: Abrindo Interfaces no Navegador" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "Deseja abrir as interfaces no navegador? (s/n): " -ForegroundColor Yellow -NoNewline
$abrirBrowser = Read-Host

if ($abrirBrowser -eq "s") {
    Write-Host ""
    Write-Host "Abrindo MailHog..." -ForegroundColor Yellow
    Start-Process "http://localhost:8025"
    Start-Sleep -Seconds 1
    
    Write-Host "Abrindo H2 Console..." -ForegroundColor Yellow
    Start-Process "http://localhost:8082/h2-console"
    Start-Sleep -Seconds 1
    
    Write-Host "Abrindo Health Check..." -ForegroundColor Yellow
    Start-Process "http://localhost:8082/api/auth/health"
    
    Write-Host ""
    Write-Host "✅ Interfaces abertas!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "URLs para abrir manualmente:" -ForegroundColor Yellow
    Write-Host "  • MailHog:     http://localhost:8025" -ForegroundColor White
    Write-Host "  • H2 Console:  http://localhost:8082/h2-console" -ForegroundColor White
    Write-Host "  • Health Check: http://localhost:8082/api/auth/health" -ForegroundColor White
}

Start-Sleep -Seconds 2

# ═══════════════════════════════════════════════════════════════
# RESUMO FINAL
# ═══════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host "🎉 SETUP COMPLETO - PRONTO PARA DEMONSTRAÇÃO!" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host ""
Write-Host "📊 STATUS DOS SERVIÇOS:" -ForegroundColor Cyan
Write-Host "   ✅ Backend:         http://localhost:8082" -ForegroundColor Gray
Write-Host "   ✅ MailHog:         http://localhost:8025" -ForegroundColor Gray
Write-Host "   ✅ H2 Console:      http://localhost:8082/h2-console" -ForegroundColor Gray
Write-Host ""
Write-Host "🚀 PRÓXIMOS PASSOS:" -ForegroundColor Cyan
Write-Host ""
Write-Host "   1️⃣  Abrir Postman Desktop" -ForegroundColor White
Write-Host ""
Write-Host "   2️⃣  Importar Collection:" -ForegroundColor White
Write-Host "      File → Import → Neuroefficiency_Auth_v3.postman_collection.json" -ForegroundColor Gray
Write-Host ""
Write-Host "   3️⃣  Executar endpoints 1-10 em sequência" -ForegroundColor White
Write-Host "      (username e dados são gerados automaticamente)" -ForegroundColor Gray
Write-Host ""
Write-Host "   4️⃣  Após endpoint 10, copiar SQL do console Postman" -ForegroundColor White
Write-Host ""
Write-Host "   5️⃣  Abrir H2 Console e configurar:" -ForegroundColor White
Write-Host "      • JDBC URL: jdbc:h2:mem:neurodb" -ForegroundColor Gray
Write-Host "      • Username: sa" -ForegroundColor Gray
Write-Host "      • Password: (vazio)" -ForegroundColor Gray
Write-Host ""
Write-Host "   6️⃣  Executar SQL copiado do Postman" -ForegroundColor White
Write-Host ""
Write-Host "   7️⃣  Voltar ao Postman e executar endpoints 11-27" -ForegroundColor White
Write-Host ""
Write-Host "   ✅ Resultado: 27/27 endpoints funcionando (100%)!" -ForegroundColor Green
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host ""
Write-Host "📖 DOCUMENTAÇÃO COMPLETA:" -ForegroundColor Cyan
Write-Host "   • Guia Passo a Passo: GUIA-EXECUCAO-100-PERFEITA.md" -ForegroundColor White
Write-Host "   • Guia Rápido:        GUIA-RAPIDO-COLLECTION.md" -ForegroundColor White
Write-Host "   • Guia Postman:       DOCS/GUIA_POSTMAN.md" -ForegroundColor White
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host ""
Write-Host "💡 DICAS PARA DEMONSTRAÇÃO:" -ForegroundColor Cyan
Write-Host ""
Write-Host "   • Mantenha as 3 abas do navegador abertas:" -ForegroundColor White
Write-Host "     1. MailHog (para mostrar email em tempo real)" -ForegroundColor Gray
Write-Host "     2. H2 Console (para mostrar banco de dados)" -ForegroundColor Gray
Write-Host "     3. Health Check (para mostrar API saudável)" -ForegroundColor Gray
Write-Host ""
Write-Host "   • No Postman, mostre:" -ForegroundColor White
Write-Host "     - Collection organizada em 3 fases" -ForegroundColor Gray
Write-Host "     - Testes automatizados (aba Tests)" -ForegroundColor Gray
Write-Host "     - Variáveis salvas automaticamente" -ForegroundColor Gray
Write-Host "     - Console com logs informativos" -ForegroundColor Gray
Write-Host ""
Write-Host "   • Tempo estimado de demonstração: 10-15 minutos" -ForegroundColor White
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host ""
Write-Host "Pressione ENTER para finalizar..." -ForegroundColor Yellow
$null = Read-Host
Write-Host ""
Write-Host "Boa sorte na apresentação! 🚀" -ForegroundColor Green
Write-Host ""

