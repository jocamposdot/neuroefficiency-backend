# 📧 GUIA RÁPIDO - Instalação MailHog do Zero

**Versão:** 1.0  
**Data:** 17 de Outubro de 2025  
**Tempo estimado:** 10-15 minutos (primeira vez)

---

## 🎯 **O QUE É MAILHOG?**

MailHog é um servidor de email para **testes** que:
- ✅ Captura emails enviados pela aplicação
- ✅ Exibe os emails em uma interface web
- ✅ **NÃO envia** emails reais (perfeito para testes!)
- ✅ É **gratuito** e **open source**

**Você precisa do MailHog para:**
- Testar a funcionalidade de recuperação de senha
- Ver os emails com tokens de reset
- Demonstrar o fluxo completo da Fase 2

---

## 🚀 **INSTALAÇÃO COMPLETA (3 PASSOS)**

### **═══════════════════════════════════════════════════════**
### **PASSO 1: Instalar Docker Desktop**
### **═══════════════════════════════════════════════════════**

#### **1.1. Verificar se já tem Docker**

**Abrir PowerShell:**
- Pressionar `Win + X`
- Selecionar "Windows PowerShell"

**Executar:**
```powershell
docker --version
```

**✅ Se aparecer:** `Docker version 24.x.x`
- **Docker JÁ está instalado!** → Pular para PASSO 2

**❌ Se aparecer erro:**
```
docker : O termo 'docker' não é reconhecido...
```
- **Docker NÃO está instalado** → Continuar abaixo

---

#### **1.2. Baixar Docker Desktop**

1. **Abrir navegador**

2. **Ir para:** https://www.docker.com/products/docker-desktop

3. **Clicar em:** **"Download for Windows"** (botão azul grande)

4. **Aguardar download:**
   - Arquivo: `Docker Desktop Installer.exe`
   - Tamanho: ~500-600 MB
   - Tempo: 5-10 minutos (depende da internet)

---

#### **1.3. Instalar Docker Desktop**

1. **Executar o instalador:**
   - Clicar duas vezes em: `Docker Desktop Installer.exe`
   - Se aparecer alerta de segurança, clicar em: **"Sim"**

2. **Tela de instalação:**
   ```
   ┌──────────────────────────────────────────┐
   │  Docker Desktop Installer                │
   ├──────────────────────────────────────────┤
   │  Configuration                           │
   │  ☑ Use WSL 2 instead of Hyper-V         │
   │  ☑ Add shortcut to desktop              │
   │                                          │
   │              [ OK ]  [ Cancel ]          │
   └──────────────────────────────────────────┘
   ```
   - **Deixar as duas opções marcadas** (padrão)
   - Clicar em: **"OK"**

3. **Aguardar instalação:**
   - Tempo: ~5 minutos
   - Barra de progresso vai aparecer

4. **Finalizar:**
   ```
   ┌──────────────────────────────────────────┐
   │  Installation succeeded                  │
   ├──────────────────────────────────────────┤
   │  Docker Desktop requires a restart       │
   │  to complete installation.               │
   │                                          │
   │        [ Close and restart ]             │
   └──────────────────────────────────────────┘
   ```
   - Clicar em: **"Close and restart"**
   - **⚠️ IMPORTANTE:** O computador vai **reiniciar**!
   - Salve tudo que estiver aberto antes!

---

#### **1.4. Após Reiniciar**

1. **Docker Desktop inicia automaticamente**
   - Você verá um ícone de baleia na **bandeja do sistema** (canto inferior direito)

2. **Aguardar ficar pronto:**
   ```
   Ícone da baleia: 🐋 (cinza) → Inicializando...
   ↓
   Ícone da baleia: 🐋 (verde) → Pronto! ✅
   ```
   - Tempo: ~30-60 segundos

3. **Verificar instalação:**
   - Abrir PowerShell novamente
   - Executar:
   ```powershell
   docker --version
   docker ps
   ```
   
   **✅ Resultado esperado:**
   ```
   Docker version 24.0.7, build afdd53b
   
   CONTAINER ID   IMAGE   COMMAND   CREATED   STATUS   PORTS   NAMES
   ```
   *(A lista estará vazia - isso é normal!)*

**✅ Docker instalado com sucesso!**

---

### **═══════════════════════════════════════════════════════**
### **PASSO 2: Baixar e Iniciar MailHog**
### **═══════════════════════════════════════════════════════**

#### **2.1. Baixar imagem do MailHog**

**No PowerShell, executar:**
```powershell
docker run -d --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

#### **2.2. O que vai acontecer (NORMAL):**

```
Unable to find image 'mailhog/mailhog:latest' locally
latest: Pulling from mailhog/mailhog
9d48c3bd43c5: Pull complete
68e7c9c42a6f: Pull complete
8e86a6bc5f6e: Pull complete
a1d93daa8f6c: Pull complete
Digest: sha256:abc123...
Status: Downloaded newer image for mailhog/mailhog:latest
abc123def456789...
```

**✅ Isso significa que:**
1. Docker está baixando a imagem do MailHog (~10 MB)
2. Criou um container chamado "mailhog"
3. Iniciou o MailHog em background
4. Retornou o ID do container (abc123def...)

**⏱️ Tempo:** ~30 segundos (primeira vez)

---

#### **2.3. Verificar se está rodando**

```powershell
docker ps
```

**✅ Resultado esperado:**
```
CONTAINER ID   IMAGE             COMMAND           CREATED         STATUS         PORTS                              NAMES
abc123def456   mailhog/mailhog   "MailHog"         30 seconds ago  Up 29 seconds  0.0.0.0:1025->1025/tcp,            mailhog
                                                                                   0.0.0.0:8025->8025/tcp
```

**Se aparecer a linha do MailHog:** ✅ **Está rodando!**

---

#### **2.4. Verificar se está acessível**

**Opção A: Navegador**
1. Abrir navegador
2. Ir para: **`http://localhost:8025`**

**✅ Você deve ver:**
```
┌──────────────────────────────────────────┐
│  MailHog                                 │
├──────────────────────────────────────────┤
│  Messages (0)                            │
│                                          │
│  [No messages yet]                       │
│                                          │
│  Waiting for emails...                   │
└──────────────────────────────────────────┘
```

**Opção B: PowerShell**
```powershell
Invoke-RestMethod -Uri "http://localhost:8025/api/v2/messages"
```

**✅ Resultado esperado:**
```
total count items
----- ----- -----
    0     0 {}
```

**✅ MailHog instalado e funcionando!**

---

### **═══════════════════════════════════════════════════════**
### **PASSO 3: Testar MailHog (Opcional)**
### **═══════════════════════════════════════════════════════**

#### **3.1. Enviar email de teste**

**Você pode testar enviando um email através da aplicação:**

1. Iniciar o backend:
   ```powershell
   cd C:\Users\rafav\Downloads\neuro-core
   ./mvnw spring-boot:run
   ```

2. No Postman, executar endpoint 6 (Password Reset Request)

3. Abrir MailHog no navegador: `http://localhost:8025`

4. **✅ Ver email aparecer em tempo real!**

---

## 🎯 **COMANDOS ÚTEIS**

### **Verificar status do MailHog:**
```powershell
docker ps | findstr mailhog
```

### **Parar MailHog:**
```powershell
docker stop mailhog
```

### **Iniciar MailHog novamente:**
```powershell
docker start mailhog
```

### **Ver logs do MailHog:**
```powershell
docker logs mailhog
```

### **Remover MailHog completamente:**
```powershell
docker stop mailhog
docker rm mailhog
```

### **Reinstalar do zero:**
```powershell
docker stop mailhog
docker rm mailhog
docker run -d --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

---

## 🔧 **TROUBLESHOOTING**

### **Problema 1: "docker: command not found"**

**Causa:** Docker não está instalado ou não foi adicionado ao PATH

**Solução:**
1. Verificar se Docker Desktop está instalado
2. Verificar se Docker Desktop está rodando (ícone na bandeja)
3. Reiniciar PowerShell
4. Se ainda não funcionar, reiniciar o computador

---

### **Problema 2: "port 8025 is already in use"**

**Causa:** Outra aplicação está usando a porta 8025

**Solução A - Usar outra porta:**
```powershell
docker run -d --name mailhog -p 1025:1025 -p 8026:8025 mailhog/mailhog
```
*(Agora acesse: http://localhost:8026)*

**Solução B - Encontrar e matar o processo:**
```powershell
netstat -ano | findstr :8025
taskkill /F /PID <PID>
```

---

### **Problema 3: "Conflict. The container name '/mailhog' is already in use"**

**Causa:** Já existe um container com nome "mailhog"

**Solução:**
```powershell
# Opção A: Iniciar o existente
docker start mailhog

# Opção B: Remover e criar novo
docker rm mailhog
docker run -d --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

---

### **Problema 4: MailHog não aparece no navegador**

**Causa:** Container não está rodando ou porta errada

**Solução:**
1. Verificar se está rodando:
   ```powershell
   docker ps | findstr mailhog
   ```

2. Se não aparecer, iniciar:
   ```powershell
   docker start mailhog
   ```

3. Verificar porta correta: `http://localhost:8025`

4. Limpar cache do navegador (`Ctrl + F5`)

---

### **Problema 5: Docker Desktop não inicia**

**Causa:** WSL 2 não está configurado ou virtualização desabilitada

**Solução:**
1. Abrir PowerShell como **Administrador**
2. Executar:
   ```powershell
   wsl --install
   ```
3. Reiniciar computador
4. Iniciar Docker Desktop novamente

---

## 🎉 **PARABÉNS!**

Você instalou e configurou:
- ✅ Docker Desktop
- ✅ MailHog
- ✅ Verificou que está funcionando

**Próximos passos:**
1. Seguir: `GUIA-EXECUCAO-100-PERFEITA.md`
2. Executar todos os 27 endpoints
3. Ver emails chegando no MailHog em tempo real!

---

## 📝 **RESUMO RÁPIDO**

### **Para Próximas Vezes:**

```powershell
# 1. Verificar se Docker está rodando
docker ps

# 2. Se MailHog não estiver rodando, iniciar:
docker start mailhog

# 3. Verificar se está acessível:
# Abrir: http://localhost:8025

# 4. Pronto! ✅
```

**Tempo:** ~10 segundos

---

## 🌐 **URLS IMPORTANTES**

| Serviço | URL | Descrição |
|---------|-----|-----------|
| **MailHog Web UI** | `http://localhost:8025` | Interface para ver emails |
| **MailHog SMTP** | `smtp://localhost:1025` | Porta onde backend envia emails |
| **MailHog API** | `http://localhost:8025/api/v2/messages` | API REST para consultar mensagens |

---

## 💡 **DICAS**

### **✅ DOs:**
- ✅ Manter Docker Desktop sempre rodando durante testes
- ✅ Verificar MailHog antes de executar endpoints
- ✅ Usar `docker ps` para verificar status
- ✅ Limpar emails antigos no MailHog (botão "Delete all")

### **❌ DON'Ts:**
- ❌ Fechar Docker Desktop durante testes
- ❌ Tentar acessar porta 1025 no navegador (é SMTP, não HTTP)
- ❌ Criar múltiplos containers MailHog (use apenas 1)

---

## 🔄 **CICLO DE USO NORMAL**

```
1. Ligar computador
   ↓
2. Docker Desktop inicia automaticamente
   ↓
3. Iniciar MailHog: docker start mailhog
   ↓
4. Verificar: http://localhost:8025
   ↓
5. Executar testes da aplicação
   ↓
6. Ver emails chegando no MailHog
   ↓
7. Quando terminar, pode deixar rodando ou parar:
   docker stop mailhog
```

---

**Criado em:** 17 de Outubro de 2025  
**Versão:** 1.0  
**Status:** ✅ Completo e Testado

---

**Agora você está pronto para testar a aplicação com 100% de funcionalidade! 🚀**

