# 🚀 Neuroefficiency - Sistema de Autenticação

**Versão:** 1.0 - Fase 1 Completa  
**Status:** ✅ 100% Funcional e Testado  
**Última Atualização:** 12 de Outubro de 2025

---

## 📋 INÍCIO RÁPIDO

### **Para Testar a API:**
1. 📦 **[GUIA_POSTMAN.md](GUIA_POSTMAN.md)** - Importar collection e testar
2. 📄 **Collection:** `Neuroefficiency_Auth.postman_collection.json`

### **Para Desenvolvedores:**
3. 📘 **[DOCS/GUIA_TÉCNICO_COMPLETO.md](DOCS/GUIA_TÉCNICO_COMPLETO.md)** - Guia técnico completo
4. 📚 **[DOCS/README.md](DOCS/README.md)** - Índice da documentação

### **Para Gerência:**
5. 🎯 **[DOCS/GUIA_DEMO_GERENCIA.md](DOCS/GUIA_DEMO_GERENCIA.md)** - Guia para apresentações

---

## 🎯 STATUS DO PROJETO

| Métrica | Valor |
|---------|-------|
| **Fase Atual** | Fase 1 - Autenticação Básica |
| **Progresso** | ✅ 100% Completo |
| **Endpoints** | 5/5 (100%) |
| **Testes** | 16/16 passando (100%) |
| **Collection Postman** | ✅ Completa e Funcional |

---

## 🚀 COMO EXECUTAR

### **1. Pré-requisitos:**
- Java 21
- Maven 3.8+

### **2. Executar Aplicação:**
```bash
# Executar via Maven
./mvnw spring-boot:run

# Aplicação estará disponível em:
http://localhost:8082
```

### **3. Testar Endpoints:**

#### **Opção A: Postman (Recomendado)**
```
1. Importar: Neuroefficiency_Auth.postman_collection.json
2. Executar endpoints na ordem numérica
3. Ver testes passando automaticamente ✅
```

#### **Opção B: cURL**
```bash
# Health Check
curl http://localhost:8082/api/auth/health

# Register
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test@1234","confirmPassword":"Test@1234"}'

# Login
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test@1234"}' \
  -c cookies.txt

# Me (usando cookie da sessão)
curl http://localhost:8082/api/auth/me -b cookies.txt

# Logout
curl -X POST http://localhost:8082/api/auth/logout -b cookies.txt
```

#### **Opção C: PowerShell (Script Automatizado)**
```powershell
# Executar script de teste completo
.\test-api.ps1
```

---

## 📦 COLLECTION POSTMAN

### **Arquivo:** `Neuroefficiency_Auth.postman_collection.json`

### **Conteúdo:**
- ✅ 5 endpoints funcionais (Health, Register, Login, Me, Logout)
- ✅ 3 endpoints de validação (testes de erro)
- ✅ Testes automatizados em todos os endpoints
- ✅ Username único gerado automaticamente
- ✅ Gerenciamento de sessão automático
- ✅ Variáveis de ambiente
- ✅ Scripts pre-request e post-request

### **Guia Completo:** [GUIA_POSTMAN.md](GUIA_POSTMAN.md)

---

## 📊 ENDPOINTS IMPLEMENTADOS

### **1. Health Check** ✅
```
GET /api/auth/health
Acesso: Público
Status: 100% Funcional
```

### **2. Register** ✅
```
POST /api/auth/register
Acesso: Público
Status: 100% Funcional
Validações: Username único, senha forte, confirmação
```

### **3. Login** ✅
```
POST /api/auth/login
Acesso: Público
Status: 100% Funcional
Cria: Sessão HTTP, Cookie JSESSIONID
```

### **4. Me - Get Current User** ✅
```
GET /api/auth/me
Acesso: Requer autenticação
Status: 100% Funcional (problema de sessão resolvido)
```

### **5. Logout** ✅
```
POST /api/auth/logout
Acesso: Requer autenticação
Status: 100% Funcional (problema de sessão resolvido)
```

---

## 🔐 SEGURANÇA

### **Implementações:**
- ✅ BCrypt força 12 (padrão para sistemas de saúde)
- ✅ Spring Security integrado
- ✅ Validação de senha forte (8+ chars, maiúscula, minúscula, número, especial)
- ✅ Sessões HTTP seguras
- ✅ SecurityContext persistido corretamente
- ✅ Sanitização de inputs (previne log injection)

### **Próximas Melhorias (Fase 2):**
- ⏳ RBAC (Role-Based Access Control)
- ⏳ Rate Limiting
- ⏳ CSRF Protection
- ⏳ HTTPS obrigatório

---

## 🧪 TESTES

### **Executar Testes:**
```bash
# Executar todos os testes
./mvnw test

# Executar com relatório detalhado
./mvnw test -Dtest=AuthenticationServiceTest

# Ver cobertura
./mvnw test jacoco:report
```

### **Resultado:**
```
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
✅ 100% SUCCESS
```

### **Cobertura:**
- ✅ Testes unitários (6)
- ✅ Testes de integração (9)
- ✅ Teste de contexto Spring (1)

---

## 📁 ESTRUTURA DO PROJETO

```
neuro-core/
├── src/
│   ├── main/
│   │   ├── java/com/neuroefficiency/
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   └── Usuario.java
│   │   │   │   └── repository/
│   │   │   │       └── UsuarioRepository.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── exception/
│   │   │   ├── security/
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   └── service/
│   │   │       └── AuthenticationService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-test.properties
│   │       └── db/migration/
│   │           └── V1__create_usuarios_table.sql
│   └── test/
│       └── java/com/neuroefficiency/
│           ├── controller/
│           └── service/
├── DOCS/
│   ├── README.md
│   ├── GUIA_TÉCNICO_COMPLETO.md
│   ├── GUIA_DEMO_GERENCIA.md
│   └── [outros documentos]
├── Neuroefficiency_Auth.postman_collection.json
├── GUIA_POSTMAN.md
├── test-api.ps1
├── pom.xml
└── README.md (este arquivo)
```

---

## 🛠️ TECNOLOGIAS

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| Java | 21 | Linguagem |
| Spring Boot | 3.5.6 | Framework |
| Spring Security | 6.2.x | Autenticação/Autorização |
| BCrypt | - | Hash de senhas |
| H2 Database | 2.3.232 | Banco em memória (dev) |
| PostgreSQL | - | Banco produção (configurado) |
| Flyway | - | Migrations |
| Lombok | - | Redução de boilerplate |
| JUnit 5 | 5.10.x | Testes |

---

## 📖 DOCUMENTAÇÃO

### **Documentos Principais:**

1. **[GUIA_POSTMAN.md](GUIA_POSTMAN.md)** - Guia da collection Postman
2. **[DOCS/GUIA_TÉCNICO_COMPLETO.md](DOCS/GUIA_TÉCNICO_COMPLETO.md)** - Guia técnico completo
3. **[DOCS/GUIA_DEMO_GERENCIA.md](DOCS/GUIA_DEMO_GERENCIA.md)** - Para apresentações
4. **[DOCS/README.md](DOCS/README.md)** - Índice completo da documentação

---

## 🎯 PRÓXIMOS PASSOS

### **Fase 2 - RBAC (2-3 semanas)**
- Implementar roles e permissões
- Autorização baseada em roles
- Endpoints de gerenciamento

### **Fase 3 - Hardening (1-2 semanas)**
- Rate Limiting
- CSRF Protection
- Session timeout
- HTTPS obrigatório

### **Fase 4 - Password Recovery (1-2 semanas)**
- Reset de senha via email
- Tokens de recuperação
- Expiração de tokens

**Detalhes:** [DOCS/GUIA_TÉCNICO_COMPLETO.md → Próximos Passos](DOCS/GUIA_TÉCNICO_COMPLETO.md#5%EF%B8%8F%E2%83%A3-pr%C3%B3ximos-passos)

---

## 📞 SUPORTE

### **Dúvidas Técnicas:**
- Ver [DOCS/GUIA_TÉCNICO_COMPLETO.md → Troubleshooting](DOCS/GUIA_TÉCNICO_COMPLETO.md#6%EF%B8%8F%E2%83%A3-troubleshooting)

### **Problemas com Collection:**
- Ver [GUIA_POSTMAN.md → Troubleshooting](GUIA_POSTMAN.md#%E2%9D%8C-troubleshooting)

### **Dúvidas de Negócio:**
- Ver [DOCS/GUIA_DEMO_GERENCIA.md → Perguntas Frequentes](DOCS/GUIA_DEMO_GERENCIA.md)

---

## 🎉 CONCLUSÃO

**Sistema de Autenticação - Fase 1:**
- ✅ **100% Completo e Funcional**
- ✅ **5/5 endpoints operacionais**
- ✅ **16/16 testes passando**
- ✅ **Collection Postman completa**
- ✅ **Documentação abrangente**
- ✅ **Pronto para Fase 2 (RBAC)**

---

**🚀 Comece agora:** [GUIA_POSTMAN.md](GUIA_POSTMAN.md)

