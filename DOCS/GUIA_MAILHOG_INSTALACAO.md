# 📧 Guia de Instalação do MailHog
## SMTP Testing Tool para Desenvolvimento

**Versão:** 1.0  
**Data:** 14 de Outubro de 2025  
**Tarefa:** Recuperação de Senha por Email (Tarefa 2)

---

## 🎯 O Que é MailHog?

MailHog é uma ferramenta de teste de SMTP para desenvolvimento que:
- ✅ Cap human:  os enviados pela aplicação
- ✅ Mostra emails em interface web bonita
- ✅ NÃO envia emails reais (seguro para dev)
- ✅ Leve e fácil de usar

**URLs:**
- **SMTP:** `localhost:1025` (porta para aplicação enviar)
- **Web UI:** `http://localhost:8025` (ver emails capturados)

---

## 🐳 OPÇÃO 1: Docker (RECOMENDADO)

### Por que Docker?
- ✅ Instalação em 1 comando
- ✅ Funciona em qualquer SO
- ✅ Fácil de iniciar/parar
- ✅ Não "suja" o sistema

### Instalação:

#### 1. **Verificar se Docker está instalado:**
```bash
docker --version
```

Se não tiver Docker:
- Windows: [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
- Mac: [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
- Linux: `sudo apt-get install docker.io` (Ubuntu/Debian)

#### 2. **Rodar MailHog:**
```bash
docker run -d \
  --name mailhog \
  -p 1025:1025 \
  -p 8025:8025 \
  mailhog/mailhog
```

**PowerShell (Windows):**
```powershell
docker run -d --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

#### 3. **Verificar se está rodando:**
```bash
docker ps | findstr mailhog
```

Deve aparecer algo como:
```
abc123def456   mailhog/mailhog   "MailHog"   Up 2 minutes   0.0.0.0:1025->1025/tcp, 0.0.0.0:8025->8025/tcp   mailhog
```

#### 4. **Acessar Web UI:**
Abrir no navegador: **http://localhost:8025**

Deve aparecer a interface do MailHog (vazia no início).

---

### Comandos Úteis (Docker):

```bash
# Parar MailHog
docker stop mailhog

# Iniciar MailHog (se já existe)
docker start mailhog

# Ver logs
docker logs mailhog

# Remover completamente
docker stop mailhog
docker rm mailhog

# Reiniciar (limpa todos os emails)
docker restart mailhog
```

---

## 💻 OPÇÃO 2: Executável (Sem Docker)

### Windows:

#### 1. **Download:**
```powershell
# Criar pasta
New-Item -Path "C:\Tools\MailHog" -ItemType Directory -Force

# Download via PowerShell
Invoke-WebRequest -Uri "https://github.com/mailhog/MailHog/releases/download/v1.0.1/MailHog_windows_amd64.exe" -OutFile "C:\Tools\MailHog\MailHog.exe"
```

Ou baixar manualmente:
[https://github.com/mailhog/MailHog/releases/download/v1.0.1/MailHog_windows_amd64.exe](https://github.com/mailhog/MailHog/releases/download/v1.0.1/MailHog_windows_amd64.exe)

#### 2. **Executar:**
```powershell
cd C:\Tools\MailHog
.\MailHog.exe
```

#### 3. **Acessar:**
Abrir: **http://localhost:8025**

---

### Linux/Mac:

#### 1. **Download:**
```bash
# Linux
wget https://github.com/mailhog/MailHog/releases/download/v1.0.1/MailHog_linux_amd64
chmod +x MailHog_linux_amd64

# Mac
wget https://github.com/mailhog/MailHog/releases/download/v1.0.1/MailHog_darwin_amd64
chmod +x MailHog_darwin_amd64
```

#### 2. **Executar:**
```bash
# Linux
./MailHog_linux_amd64

# Mac
./MailHog_darwin_amd64
```

#### 3. **Acessar:**
Abrir: **http://localhost:8025**

---

## ✅ TESTANDO A INSTALAÇÃO

### 1. **MailHog deve estar rodando**
Verificar em: **http://localhost:8025**

### 2. **Backend configurado**
Verificar `application-dev.properties`:
```properties
spring.mail.host=localhost
spring.mail.port=1025
```

### 3. **Rodar aplicação**
```bash
./mvnw spring-boot:run
```

### 4. **Testar envio de email** (quando implementado)
- Usar endpoint de reset de senha
- Email deve aparecer no MailHog UI

---

## 🎨 INTERFACE DO MAILHOG

### O que você verá:

**Tela inicial (sem emails):**
```
╔════════════════════════════════════════╗
║           MailHog v1.0.1               ║
╠════════════════════════════════════════╣
║                                        ║
║      Nenhum email capturado ainda     ║
║                                        ║
╚════════════════════════════════════════╝
```

**Com emails:**
```
╔════════════════════════════════════════╗
║  De: noreply@neuroefficiency.local    ║
║  Para: user@example.com                ║
║  Assunto: Redefinir sua senha          ║
║  Data: 2025-10-14 15:30:00             ║
╠════════════════════════════════════════╣
║  Conteúdo do email...                  ║
╚════════════════════════════════════════╝
```

Você pode:
- ✅ Ver todos os emails
- ✅ Clicar para ver detalhes
- ✅ Ver HTML e texto simples
- ✅ Ver headers completos
- ✅ Deletar emails
- ✅ Buscar emails

---

## 🔧 TROUBLESHOOTING

### ❌ Erro: "Port already in use"

**Causa:** Porta 1025 ou 8025 já está em uso.

**Solução 1 - Encontrar o processo:**
```bash
# Windows
netstat -ano | findstr :1025
netstat -ano | findstr :8025

# Linux/Mac
lsof -i :1025
lsof -i :8025
```

**Solução 2 - Mudar portas (se necessário):**
```bash
# Docker com portas diferentes
docker run -d --name mailhog -p 2025:1025 -p 9025:8025 mailhog/mailhog

# Atualizar application-dev.properties
spring.mail.port=2025
```

---

### ❌ Docker não inicia

**Causa:** Docker não está rodando.

**Solução Windows:**
1. Abrir Docker Desktop
2. Aguardar inicialização
3. Rodar comando novamente

**Solução Linux:**
```bash
sudo systemctl start docker
```

---

### ❌ Emails não aparecem no MailHog

**Possíveis causas:**

1. **Backend não enviou email**
   - Verificar logs do backend
   - Verificar se serviço de email está configurado

2. **Porta errada**
   - Verificar: `spring.mail.port=1025`

3. **MailHog não está rodando**
   - Verificar: `http://localhost:8025`

---

## 📚 DOCUMENTAÇÃO OFICIAL

- **GitHub:** [https://github.com/mailhog/MailHog](https://github.com/mailhog/MailHog)
- **Releases:** [https://github.com/mailhog/MailHog/releases](https://github.com/mailhog/MailHog/releases)

---

## ✅ CHECKLIST DE INSTALAÇÃO

Antes de continuar com a Tarefa 2:

- [ ] MailHog instalado (Docker ou executável)
- [ ] MailHog rodando (verificar localhost:8025)
- [ ] Porta 1025 disponível (SMTP)
- [ ] Porta 8025 disponível (Web UI)
- [ ] `application-dev.properties` configurado
- [ ] Backend pode iniciar sem erros

---

## 🚀 PRÓXIMOS PASSOS

Com MailHog rodando, você está pronto para:
1. ✅ Implementar EmailService
2. ✅ Criar templates de email
3. ✅ Testar envio de emails
4. ✅ Ver emails capturados no MailHog UI

---

**Preparado por:** Neuroefficiency Team  
**Data:** 14 de Outubro de 2025  
**Tarefa:** 2 - Recuperação de Senha por Email

