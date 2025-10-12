# 📦 Guia da Collection Postman - Neuroefficiency Auth

**Versão Collection:** 1.1  
**Status:** ✅ 100% Funcional  
**Última Atualização:** 12 de Outubro de 2025

---

## 🚀 INÍCIO RÁPIDO

### **1. Importar Collection**

1. Abrir Postman
2. Clicar em `File` → `Import` (ou pressionar `Ctrl+O`)
3. Selecionar o arquivo `Neuroefficiency_Auth.postman_collection.json` na raiz do projeto
4. ✅ **Pronto!** A collection está configurada e pronta para uso (zero configuração necessária)

### **2. Executar Aplicação**

```bash
# Na raiz do projeto
./mvnw spring-boot:run

# Aguardar mensagem:
# Started NeuroefficiencyApplication in X seconds
```

### **3. Testar Endpoints**

Execute os endpoints **na ordem numérica** (1 → 2 → 3 → 4 → 5):

1. **Health Check** - Verificar se API está UP
2. **Register** - Criar novo usuário (username único gerado automaticamente)
3. **Login** - Autenticar usuário (sessão criada automaticamente)
4. **Me** - Obter dados do usuário atual (requer autenticação)
5. **Logout** - Encerrar sessão

---

## 📋 ENDPOINTS DA COLLECTION

### **1. Health Check** ✅

**Descrição:** Verifica se o serviço de autenticação está disponível e operacional.

- **Método:** `GET`
- **URL:** `http://localhost:8082/api/auth/health`
- **Acesso:** Público (não requer autenticação)

**Resposta Esperada (200 OK):**
```json
{
  "status": "UP",
  "service": "Authentication Service",
  "version": "1.0"
}
```

**Testes Automatizados:**
- ✅ Status code é 200
- ✅ Resposta tem estrutura correta
- ✅ Serviço está UP

---

### **2. Register - Novo Usuário** ✅

**Descrição:** Registra um novo usuário no sistema com validações completas.

- **Método:** `POST`
- **URL:** `http://localhost:8082/api/auth/register`
- **Acesso:** Público

**Body (JSON):**
```json
{
  "username": "testuser_1728737284123",
  "password": "Test@1234",
  "confirmPassword": "Test@1234"
}
```

**Funcionalidade Automática:**
- ✅ Username único gerado automaticamente com timestamp
- ✅ Variáveis de collection atualizadas automaticamente
- ✅ User ID e username salvos para próximos testes

**Resposta Esperada (201 Created):**
```json
{
  "message": "Usuário registrado com sucesso",
  "user": {
    "id": 1,
    "username": "testuser_1728737284123",
    "enabled": true,
    "createdAt": "2025-10-12T09:00:00"
  }
}
```

**Validações:**
- ✅ Username: 3-50 caracteres
- ✅ Password: 8+ caracteres, maiúscula, minúscula, número, especial
- ✅ Password e ConfirmPassword devem ser iguais
- ✅ Username único (não pode duplicar)

**Testes Automatizados:**
- ✅ Status code é 201
- ✅ Estrutura de resposta correta
- ✅ Registro bem-sucedido
- ✅ Username corresponde ao enviado
- ✅ User ID salvo em variável

---

### **3. Login** ✅

**Descrição:** Autentica o usuário e cria uma sessão HTTP.

- **Método:** `POST`
- **URL:** `http://localhost:8082/api/auth/login`
- **Acesso:** Público

**Body (JSON):**
```json
{
  "username": "{{username}}",
  "password": "{{password}}"
}
```

**Funcionalidade Automática:**
- ✅ Username e password carregados das variáveis
- ✅ Cookie JSESSIONID capturado automaticamente
- ✅ Sessão HTTP criada e persistida
- ✅ SecurityContext salvo automaticamente

**Resposta Esperada (200 OK):**
```json
{
  "message": "Login realizado com sucesso",
  "user": {
    "id": 1,
    "username": "testuser_1728737284123",
    "enabled": true,
    "createdAt": "2025-10-12T09:00:00"
  }
}
```

**Headers da Resposta:**
- ✅ `Set-Cookie: JSESSIONID=XXXXXX...`

**Testes Automatizados:**
- ✅ Status code é 200
- ✅ Estrutura de resposta correta
- ✅ Login bem-sucedido
- ✅ Cookie JSESSIONID presente
- ✅ Username correto

---

### **4. Me - Get Current User** ✅

**Descrição:** Obtém os dados do usuário atualmente autenticado.

- **Método:** `GET`
- **URL:** `http://localhost:8082/api/auth/me`
- **Acesso:** 🔒 **Requer Autenticação** (cookie JSESSIONID do login)

**Headers Automáticos:**
- ✅ Cookie JSESSIONID enviado automaticamente pelo Postman

**Resposta Esperada (200 OK):**
```json
{
  "id": 1,
  "username": "testuser_1728737284123",
  "enabled": true,
  "createdAt": "2025-10-12T09:00:00"
}
```

**Testes Automatizados:**
- ✅ Status code é 200
- ✅ Estrutura de resposta correta
- ✅ Username corresponde ao registrado

**⚠️ Atenção:**
- Se retornar **403 Forbidden**, execute o endpoint **3. Login** novamente
- Cookie JSESSIONID é gerenciado automaticamente pelo Postman

---

### **5. Logout** ✅

**Descrição:** Encerra a sessão do usuário atual.

- **Método:** `POST`
- **URL:** `http://localhost:8082/api/auth/logout`
- **Acesso:** 🔒 **Requer Autenticação** (cookie JSESSIONID do login)

**Headers Automáticos:**
- ✅ Cookie JSESSIONID enviado automaticamente pelo Postman

**Resposta Esperada (200 OK):**
```json
{
  "message": "Logout realizado com sucesso"
}
```

**Funcionalidade:**
- ✅ Sessão HTTP invalidada
- ✅ SecurityContext limpo
- ✅ Cookie JSESSIONID removido

**Testes Automatizados:**
- ✅ Status code é 200
- ✅ Estrutura de resposta correta
- ✅ Mensagem de sucesso

---

## 🧪 ENDPOINTS DE VALIDAÇÃO (TESTES DE ERRO)

A collection também inclui endpoints para testar cenários de erro:

### **6. Register - Username Duplicado**
- Tenta registrar com username já existente
- **Resposta esperada:** 409 Conflict

### **7. Register - Validações**
- Testa campos vazios, senha fraca, etc.
- **Resposta esperada:** 400 Bad Request

### **8. Login - Credenciais Inválidas**
- Tenta login com senha incorreta
- **Resposta esperada:** 401 Unauthorized

---

## 🔄 FLUXO COMPLETO

### **Cenário 1: Fluxo Normal (Sucesso)**

```
1. Health Check → 200 OK ✅
2. Register → 201 Created ✅
3. Login → 200 OK + JSESSIONID ✅
4. Me → 200 OK (dados do usuário) ✅
5. Logout → 200 OK ✅
6. Me → 403 Forbidden (sem autenticação) ✅
```

### **Cenário 2: Testes de Validação**

```
1. Register (duplicado) → 409 Conflict ✅
2. Register (senha fraca) → 400 Bad Request ✅
3. Login (senha errada) → 401 Unauthorized ✅
```

---

## 📊 VARIÁVEIS DE COLLECTION

A collection v1.1 **não requer environment**. As variáveis são armazenadas na própria collection:

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `baseUrl` | URL base da API | `http://localhost:8082` |
| `username` | Username gerado | `testuser_1728737284123` |
| `password` | Password padrão | `Test@1234` |
| `userId` | ID do usuário criado | `1` |

**Atualização Automática:**
- ✅ `username` atualizado no **pre-request** do Register (timestamp único)
- ✅ `userId` e `username` atualizados no **post-response** do Register
- ✅ Sem necessidade de editar manualmente

---

## ✅ TESTES AUTOMATIZADOS

### **Resumo dos Testes:**

| Endpoint | Testes | Status |
|----------|--------|--------|
| **Health Check** | 3 testes | ✅ |
| **Register** | 4 testes | ✅ |
| **Login** | 5 testes | ✅ |
| **Me** | 3 testes | ✅ |
| **Logout** | 3 testes | ✅ |
| **Validações** | 3 testes | ✅ |
| **TOTAL** | **21 testes** | ✅ **100%** |

### **Executar Todos os Testes:**

1. Clicar com botão direito na collection **"Neuroefficiency Auth API - Completa"**
2. Selecionar **"Run collection"**
3. Clicar em **"Run Neuroefficiency Auth API"**
4. ✅ Ver todos os testes passando em verde

**Resultado Esperado:**
```
✅ 21/21 tests passed
✅ 8/8 requests successful
⏱️ Tempo: ~2-3 segundos
```

---

## ❌ TROUBLESHOOTING

### **Problema 1: Erro de Conexão**

**Sintoma:**
```
Error: connect ECONNREFUSED 127.0.0.1:8082
```

**Solução:**
1. Verificar se a aplicação está rodando:
   ```bash
   ./mvnw spring-boot:run
   ```
2. Aguardar mensagem: `Started NeuroefficiencyApplication`
3. Verificar se porta 8082 está livre:
   ```bash
   # Windows PowerShell
   netstat -ano | findstr :8082
   ```

---

### **Problema 2: 403 Forbidden no /me ou /logout**

**Sintoma:**
```json
{
  "timestamp": "2025-10-12T12:00:00",
  "status": 403,
  "error": "Forbidden"
}
```

**Causa:** Cookie JSESSIONID expirado ou não presente.

**Solução:**
1. Executar endpoint **3. Login** novamente
2. Aguardar resposta com `Set-Cookie: JSESSIONID=...`
3. Tentar **4. Me** ou **5. Logout** novamente
4. ✅ Deve funcionar

**Nota:** O Postman gerencia cookies automaticamente. Se o problema persistir:
- `Postman Settings` → `General` → Habilitar **"Automatically follow redirects"**
- `Postman Settings` → `General` → Habilitar **"Send cookies"**

---

### **Problema 3: 409 Conflict no Register**

**Sintoma:**
```json
{
  "error": "Username already exists",
  "message": "Username 'testuser' already exists",
  "status": 409
}
```

**Causa:** Username já existe no banco (normal se já executou a collection antes).

**Solução:**
1. **Opção A (Recomendada):** Reiniciar a aplicação (banco H2 em memória será zerado)
   ```bash
   # Parar aplicação (Ctrl+C)
   ./mvnw spring-boot:run
   ```

2. **Opção B:** O username é gerado com timestamp único. Apenas execute novamente - um novo username será criado automaticamente.

3. **Opção C:** Editar o pre-request script do endpoint **2. Register** para forçar novo timestamp

---

### **Problema 4: Testes Falhando**

**Sintoma:** Alguns testes aparecem em vermelho (failed) no Test Results.

**Causa:** Ordem de execução incorreta ou banco com dados anteriores.

**Solução:**
1. Reiniciar aplicação (limpar banco H2)
2. Executar endpoints **na ordem numérica** (1 → 2 → 3 → 4 → 5)
3. Se usar "Run collection", garantir que a ordem está preservada

---

### **Problema 5: Password Validation Error**

**Sintoma:**
```json
{
  "error": "Validation error",
  "details": {
    "password": "Password deve conter pelo menos uma letra maiúscula..."
  }
}
```

**Causa:** Password não atende aos requisitos de segurança.

**Requisitos:**
- ✅ Mínimo 8 caracteres
- ✅ Pelo menos 1 letra maiúscula
- ✅ Pelo menos 1 letra minúscula
- ✅ Pelo menos 1 número
- ✅ Pelo menos 1 caractere especial (@$!%*?&)

**Exemplo válido:** `Test@1234`

---

## 🔐 SEGURANÇA

### **Implementações:**

- ✅ **BCrypt força 12:** Hashing seguro de senhas
- ✅ **Spring Security:** Autenticação e autorização
- ✅ **Sessões HTTP:** Gerenciamento seguro de sessões
- ✅ **SecurityContext:** Persistência de contexto de segurança
- ✅ **Validações completas:** Inputs sanitizados e validados
- ✅ **Cookies HttpOnly:** JSESSIONID não acessível via JavaScript

### **Próximas Implementações (Fase 2):**

- ⏳ RBAC (Role-Based Access Control)
- ⏳ Rate Limiting
- ⏳ CSRF Protection
- ⏳ HTTPS obrigatório

---

## 📖 DOCUMENTAÇÃO ADICIONAL

### **Para Desenvolvedores:**
- 📘 **[DOCS/GUIA_TÉCNICO_COMPLETO.md](DOCS/GUIA_TÉCNICO_COMPLETO.md)** - Guia técnico detalhado
- 📄 **[DOCS/Implementação Sistema de Autenticação](DOCS/Implementação%20Sistema%20de%20Autenticação%20-%20Documentação%20Técnica%20-%202025-10-11.md)** - Código-fonte completo

### **Para Gerência:**
- 🎯 **[DOCS/GUIA_DEMO_GERENCIA.md](DOCS/GUIA_DEMO_GERENCIA.md)** - Roteiro de apresentação

### **Índice Geral:**
- 📚 **[DOCS/README.md](DOCS/README.md)** - Índice completo da documentação

---

## 🎯 MÉTRICAS DA COLLECTION

| Métrica | Valor |
|---------|-------|
| **Versão** | 1.1 |
| **Endpoints Funcionais** | 5/5 (100%) |
| **Endpoints de Validação** | 3 |
| **Total de Endpoints** | 8 |
| **Testes Automatizados** | 21 |
| **Taxa de Sucesso** | 100% ✅ |
| **Configuração Necessária** | Zero |
| **Dependências** | Nenhuma (variáveis internas) |

---

## 🚀 PRÓXIMOS PASSOS

Após testar a collection, você pode:

1. **Explorar o Código:**
   - Ver implementação em [DOCS/GUIA_TÉCNICO_COMPLETO.md](DOCS/GUIA_TÉCNICO_COMPLETO.md)

2. **Executar Testes Automatizados:**
   ```bash
   ./mvnw test
   ```

3. **Apresentar para Gerência:**
   - Usar [DOCS/GUIA_DEMO_GERENCIA.md](DOCS/GUIA_DEMO_GERENCIA.md)

4. **Contribuir:**
   - Próxima fase: RBAC (Role-Based Access Control)

---

## 📞 SUPORTE

### **Dúvidas sobre a Collection:**
- Verificar seção [❌ Troubleshooting](#-troubleshooting) deste guia

### **Dúvidas Técnicas:**
- Ver [DOCS/GUIA_TÉCNICO_COMPLETO.md → Troubleshooting](DOCS/GUIA_TÉCNICO_COMPLETO.md#6%EF%B8%8F%E2%83%A3-troubleshooting)

### **Problemas de Autenticação:**
- Ver [DOCS/GUIA_TÉCNICO_COMPLETO.md → Solução de Persistência de Sessão](DOCS/GUIA_TÉCNICO_COMPLETO.md#3%EF%B8%8F%E2%83%A3-solu%C3%A7%C3%A3o-de-persist%C3%AAncia-de-sess%C3%A3o)

---

## 🎉 CONCLUSÃO

A collection Postman **Neuroefficiency Auth API v1.1** está:

- ✅ **100% Funcional** - Todos os 5 endpoints operacionais
- ✅ **Zero Configuração** - Variáveis internas, sem environment necessário
- ✅ **Testes Automatizados** - 21 testes cobrindo todos os cenários
- ✅ **Pronta para Demo** - Interface amigável e intuitiva
- ✅ **Documentada** - Este guia e documentação técnica completa

---

**🚀 Comece agora:** [Importar Collection](#-início-rápido)

**📘 Documentação Técnica:** [DOCS/GUIA_TÉCNICO_COMPLETO.md](DOCS/GUIA_TÉCNICO_COMPLETO.md)

**🎯 Apresentar para Gerência:** [DOCS/GUIA_DEMO_GERENCIA.md](DOCS/GUIA_DEMO_GERENCIA.md)

---

**Última Atualização:** 12 de Outubro de 2025  
**Versão:** 1.0  
**Status:** ✅ Completo

