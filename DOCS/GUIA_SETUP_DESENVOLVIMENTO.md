# 🛠️ GUIA DE SETUP - Desenvolvimento
## Ambiente Completo para Desenvolvimento

**Versão:** 3.0 (Fase 2)  
**Última Atualização:** 14 de Outubro de 2025  
**Tempo Estimado:** 15 minutos

---

## 📋 PRÉ-REQUISITOS

### Obrigatórios
- ✅ **Java 21** (OpenJDK ou Oracle JDK)
- ✅ **Maven 3.9+** (incluído no projeto via wrapper)
- ✅ **Git** para clonar repositório

### Opcionais (Recomendados)
- 🔧 **IDE:** IntelliJ IDEA, VS Code, ou Eclipse
- 📧 **MailHog** para testar emails (recuperação de senha)
- 🔍 **Postman** para testar API
- 🐳 **Docker** (opcional, para MailHog)

---

## 🚀 SETUP RÁPIDO (3 PASSOS)

### Passo 1: Clonar Repositório
```bash
git clone https://github.com/jocamposdot/neuroefficiency-backend.git
cd neuroefficiency-backend
```

### Passo 2: Executar Aplicação
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Passo 3: Testar
```bash
# Health Check
curl http://localhost:8082/api/auth/health
```

**✅ Pronto!** Backend rodando em `http://localhost:8082`

---

## 📧 CONFIGURAR MAILHOG (Para Testar Emails)

### Opção A: Com Docker (Recomendado)

#### 1. Instalar Docker Desktop
- **Windows/Mac:** https://www.docker.com/products/docker-desktop
- **Linux:** https://docs.docker.com/engine/install/

#### 2. Executar MailHog
```bash
docker run -d --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

#### 3. Verificar
- **Web UI:** http://localhost:8025
- **SMTP:** localhost:1025 (já configurado no backend)

**✅ Backend já está configurado!** Apenas execute o container.

---

### Opção B: Executável Standalone (Windows)

#### 1. Baixar MailHog
```powershell
# Criar pasta
New-Item -Path "$env:USERPROFILE\MailHog" -ItemType Directory -Force

# Baixar executável
Invoke-WebRequest -Uri "https://github.com/mailhog/MailHog/releases/download/v1.0.1/MailHog_windows_amd64.exe" -OutFile "$env:USERPROFILE\MailHog\MailHog.exe"
```

#### 2. Executar
```powershell
& "$env:USERPROFILE\MailHog\MailHog.exe"
```

#### 3. Acessar
- **Web UI:** http://localhost:8025

---

### Opção C: Binário Linux/Mac

#### Linux
```bash
# Baixar
wget https://github.com/mailhog/MailHog/releases/download/v1.0.1/MailHog_linux_amd64

# Tornar executável
chmod +x MailHog_linux_amd64

# Executar
./MailHog_linux_amd64
```

#### Mac
```bash
# Via Homebrew
brew install mailhog

# Executar
mailhog
```

---

## 🗄️ BANCO DE DADOS

### H2 (Desenvolvimento - Padrão)
```
✅ Configurado automaticamente
✅ Em memória (reinicia quando o backend para)
✅ Console disponível em: http://localhost:8082/h2-console

Configurações:
- JDBC URL: jdbc:h2:mem:neurodb
- User: sa
- Password: (vazio)
```

### PostgreSQL (Produção)
```properties
# src/main/resources/application-prod.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/neuroefficiency
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

**Migrations:** Flyway gerencia automaticamente

---

## ⚙️ CONFIGURAÇÕES

### Profiles Spring

#### Development (Padrão)
```bash
# Já ativo por padrão
.\mvnw.cmd spring-boot:run
```

**Configurações:**
- Porta: 8082
- H2 Database
- MailHog SMTP (localhost:1025)
- Logs: DEBUG

#### Test
```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test
```

**Configurações:**
- Porta: 8082
- H2 Database
- Mock SMTP (sem envio real)
- Logs: INFO

#### Production
```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```

**Configurações:**
- Porta: 8080
- PostgreSQL
- SMTP Real (configurar)
- Logs: WARN

---

## 🔧 TROUBLESHOOTING

### Problema: Porta 8082 já em uso
```bash
# Windows
netstat -ano | findstr :8082
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8082 | xargs kill -9
```

### Problema: MailHog não recebe emails
**Verificar:**
1. MailHog rodando? `http://localhost:8025`
2. Porta 1025 disponível? `netstat -ano | findstr :1025`
3. Backend usando profile `dev`? (configuração correta)

**Solução:**
```bash
# Reiniciar MailHog
docker restart mailhog

# Ou executável
# Parar (Ctrl+C) e iniciar novamente
```

### Problema: Migrations não executam
**Erro:** `Schema validation failed`

**Solução:**
```bash
# Limpar banco H2 (em memória)
# Basta reiniciar o backend

# PostgreSQL - rodar migrations manualmente
.\mvnw.cmd flyway:migrate
```

### Problema: Não consigo acessar H2 Console
**Verificar:**
1. URL correta: `http://localhost:8082/h2-console`
2. JDBC URL: `jdbc:h2:mem:neurodb`
3. User: `sa`
4. Password: (deixar vazio)

### Problema: Java não encontrado
```bash
# Verificar instalação
java -version

# Deve retornar Java 21
```

**Solução:** Instalar Java 21
- **Windows:** https://adoptium.net/
- **Linux:** `sudo apt install openjdk-21-jdk`
- **Mac:** `brew install openjdk@21`

---

## 📁 ESTRUTURA DO PROJETO

```
neuroefficiency-backend/
├── src/
│   ├── main/
│   │   ├── java/com/neuroefficiency/
│   │   │   ├── config/          # Configurações (Security, I18n)
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── domain/          # Entidades e Repositories
│   │   │   ├── dto/             # DTOs (Request/Response)
│   │   │   ├── exception/       # Exceptions customizadas
│   │   │   ├── security/        # Security (UserDetails)
│   │   │   ├── service/         # Lógica de negócio
│   │   │   └── util/            # Utilitários
│   │   │
│   │   └── resources/
│   │       ├── application.properties          # Config padrão
│   │       ├── application-dev.properties      # Config dev
│   │       ├── application-test.properties     # Config test
│   │       ├── application-prod.properties     # Config prod
│   │       ├── messages_*.properties           # i18n
│   │       ├── templates/email/                # Templates email
│   │       └── db/migration/                   # Flyway migrations
│   │
│   └── test/                    # Testes (futuros)
│
├── DOCS/                        # Documentação técnica
├── pom.xml                      # Dependências Maven
├── mvnw / mvnw.cmd             # Maven Wrapper
└── README.md                    # Início rápido
```

---

## 🧪 EXECUTAR TESTES

### Testes Manuais com Scripts
```powershell
# Teste completo automatizado (recomendado)
.\test-complete-auto.ps1

# Teste simples rápido
.\test-simple.ps1

# Criar usuário novo (evita rate limiting)
.\test-fresh.ps1
```

### Ver Todos os Scripts
```powershell
Get-ChildItem *.ps1 | Select-Object Name
```

**Documentação:** Ver `DOCS/GUIA_TESTES.md`

---

## 📚 PRÓXIMOS PASSOS

### Após Setup
1. ✅ Testar API com Postman
   - Importar: `Neuroefficiency_Auth.postman_collection.json`
   - Ver: `DOCS/GUIA_POSTMAN.md`

2. ✅ Entender arquitetura
   - Ler: `DOCS/GUIA_TÉCNICO_COMPLETO.md`

3. ✅ Executar testes manuais
   - Ler: `DOCS/GUIA_TESTES.md`

### Para Desenvolvimento
1. Configurar IDE
2. Instalar Lombok plugin
3. Configurar formatação (opcional)
4. Ler decisões técnicas: `DOCS/TAREFA-2-REFERENCIA.md`

---

## 🔗 LINKS ÚTEIS

### Locais
- **Backend:** http://localhost:8082
- **Health Check:** http://localhost:8082/api/auth/health
- **H2 Console:** http://localhost:8082/h2-console
- **MailHog:** http://localhost:8025

### Documentação
- **Guia Técnico:** DOCS/GUIA_TÉCNICO_COMPLETO.md
- **Testes:** DOCS/GUIA_TESTES.md
- **Postman:** DOCS/GUIA_POSTMAN.md
- **Changelog:** DOCS/CHANGELOG.md

### Repositório
- **GitHub:** https://github.com/jocamposdot/neuroefficiency-backend

---

## ⚡ COMANDOS RÁPIDOS

```bash
# Iniciar backend
.\mvnw.cmd spring-boot:run

# Limpar e compilar
.\mvnw.cmd clean install

# Executar testes (quando implementados)
.\mvnw.cmd test

# Ver logs
# (aparecem no console automaticamente)

# Parar backend
# Ctrl+C no terminal
```

---

## 💡 DICAS

### Performance
- Use profile `dev` para desenvolvimento (logs detalhados)
- H2 é mais rápido que PostgreSQL para testes locais
- Reinicie o backend após mudanças no código

### Debugging
- Configure breakpoints na IDE
- Logs em `DEBUG` no profile dev
- Use H2 Console para verificar dados

### Boas Práticas
- Sempre teste localmente antes de commitar
- Use scripts PowerShell para testes repetitivos
- Mantenha MailHog rodando durante desenvolvimento de features de email

---

**Dúvidas?** Ver troubleshooting acima ou `DOCS/GUIA_TÉCNICO_COMPLETO.md`

✅ **Setup Completo!** Comece a desenvolver! 🚀

