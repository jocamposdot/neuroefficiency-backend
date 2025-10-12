# 📦 Guia Rápido - Collection Postman

**Arquivo:** `Neuroefficiency_Auth.postman_collection.json`  
**Versão:** 1.1 - Atualizada (Variáveis de Collection)  
**Data:** 12 de Outubro de 2025  
**Status:** ✅ 100% Funcional e Testada  
**Configuração:** ✅ Zero configuração necessária (não requer Environment)

---

## 🚀 IMPORTAÇÃO NO POSTMAN

### **Passo a Passo:**

1. **Abrir Postman**
2. **Clicar em `Import`** (canto superior esquerdo)
3. **Selecionar arquivo:** `Neuroefficiency_Auth.postman_collection.json`
4. **Clicar em `Import`**
5. **Pronto!** Collection aparecerá no painel esquerdo

---

## 📋 ESTRUTURA DA COLLECTION (8 Endpoints)

### **Endpoints Funcionais (5):**

1. ✅ **Health Check** - Verificar disponibilidade
2. ✅ **Register** - Criar novo usuário (username único gerado automaticamente)
3. ✅ **Login** - Autenticar e criar sessão
4. ✅ **Me** - Obter dados do usuário autenticado
5. ✅ **Logout** - Encerrar sessão

### **Endpoints de Validação (3):**

6. 🧪 **Validação - Username Duplicado** - Testa rejeição de username existente
7. 🧪 **Validação - Senha Fraca** - Testa requisitos de senha
8. 🧪 **Validação - Credenciais Inválidas** - Testa login com dados incorretos

---

## ⚡ EXECUÇÃO RÁPIDA

### **Opção 1: Ordem Recomendada (Fluxo Completo)**

Execute na ordem numérica:

```
1. Health Check        → Verifica se serviço está UP
2. Register           → Cria usuário (username auto-gerado)
3. Login              → Autentica e cria sessão
4. Me                 → Valida sessão funcionando
5. Logout             → Encerra sessão
```

**Resultado Esperado:** ✅ Todos os 5 testes passam

---

### **Opção 2: Testes de Validação**

Após executar o fluxo completo, testar validações:

```
6. Validação - Username Duplicado     → Deve retornar 400
7. Validação - Senha Fraca            → Deve retornar 400
8. Validação - Credenciais Inválidas  → Deve retornar 401
```

**Resultado Esperado:** ✅ Todos os erros são capturados corretamente

---

## 🎯 FUNCIONALIDADES AUTOMÁTICAS

### **1. Username Único Automático**
- O endpoint `2. Register` gera username único automaticamente
- Formato: `testuser{timestamp}`
- Exemplo: `testuser1697123456789`
- **Benefício:** Sem conflito de username duplicado

### **2. Gerenciamento de Sessão Automático**
- Postman gerencia cookie `JSESSIONID` automaticamente
- Após login, cookie é usado em `/me` e `/logout`
- **Você não precisa fazer nada!**

### **3. Variáveis da Collection**
A collection salva automaticamente:
- `testUsername` - Username gerado
- `userId` - ID do usuário criado
- `sessionId` - ID da sessão após login
- `username` - Username do usuário autenticado

**Ver variáveis:** Clicar na collection → aba `Variables` (ou ícone de olho no canto superior direito)

### **4. Testes Automatizados**
Cada endpoint tem testes que validam:
- ✅ Status code correto
- ✅ Estrutura da resposta
- ✅ Dados retornados
- ✅ Presença de cookies (quando aplicável)

**Ver resultados:** Aba `Test Results` após executar endpoint

### **5. Logs no Console**
Todos os endpoints registram logs:
- ✅ Sucesso: `console.log("✅ ...")`
- ⚠️ Avisos: `console.log("⚠️ ...")`
- 📝 Informações: `console.log("📝 ...")`

**Ver console:** `View` → `Show Postman Console` (Ctrl+Alt+C)

---

## 🔧 CONFIGURAÇÃO

### **Variável `baseUrl`:**
```
http://localhost:8082
```

**Alterar porta (se necessário):**
1. Clicar na collection
2. Ir em `Variables`
3. Alterar `baseUrl` para nova porta
4. Exemplo: `http://localhost:8083`

---

## 📊 EXEMPLO DE EXECUÇÃO COMPLETA

### **1️⃣ Health Check**
```json
GET /api/auth/health

Response (200):
{
  "status": "UP",
  "service": "Authentication Service",
  "version": "1.0"
}

✅ Test Results: 3/3 passed
```

---

### **2️⃣ Register**
```json
POST /api/auth/register

Body:
{
  "username": "testuser1697123456789",  // Auto-gerado
  "password": "Test@1234",
  "confirmPassword": "Test@1234"
}

Response (201):
{
  "message": "Usuário registrado com sucesso",
  "user": {
    "id": 1,
    "username": "testuser1697123456789",
    "enabled": true,
    "createdAt": "2025-10-12T03:16:09.222884",
    "updatedAt": "2025-10-12T03:16:09.222884"
  }
}

✅ Test Results: 4/4 passed
📝 Variables saved: userId=1, username=testuser1697123456789
```

---

### **3️⃣ Login**
```json
POST /api/auth/login

Body:
{
  "username": "testuser1697123456789",
  "password": "Test@1234"
}

Response (200):
{
  "message": "Login realizado com sucesso",
  "sessionId": "96C2E33AAFAA927E86AEBD0B9F0AF24A",
  "user": {
    "id": 1,
    "username": "testuser1697123456789",
    "enabled": true,
    "createdAt": "2025-10-12T03:16:09.222884",
    "updatedAt": "2025-10-12T03:16:09.222884"
  }
}

Cookies: JSESSIONID=96C2E33AAFAA927E86AEBD0B9F0AF24A

✅ Test Results: 5/5 passed
🔑 SessionId saved
```

---

### **4️⃣ Me - Get Current User**
```json
GET /api/auth/me

Headers: Cookie: JSESSIONID=... (automático)

Response (200):
{
  "id": 1,
  "username": "testuser1697123456789",
  "enabled": true,
  "createdAt": "2025-10-12T03:16:09.222884",
  "updatedAt": "2025-10-12T03:16:09.222884"
}

✅ Test Results: 4/4 passed
👤 User data retrieved successfully
```

---

### **5️⃣ Logout**
```json
POST /api/auth/logout

Headers: Cookie: JSESSIONID=... (automático)

Response (200):
{
  "message": "Logout realizado com sucesso"
}

✅ Test Results: 3/3 passed
🧹 Environment variables cleared
```

---

## ❌ TROUBLESHOOTING

### **Problema: 401 no Login - Username literal `{{testUsername}}`**

**Sintoma:** Login retorna 401 com mensagem "Username ou password incorretos", e no console/logs você vê que o username enviado é literalmente `{{testUsername}}` ao invés do valor gerado.

**Causa:** Versões antigas desta collection usavam `pm.environment.set()` que requer um Environment configurado no Postman. Sem environment ativo, as variáveis não são substituídas.

**Solução:**
1. **Reimportar a collection atualizada** (versão corrigida usa `pm.collectionVariables`)
2. OU criar um Environment no Postman:
   - Clicar em `Environments` (painel esquerdo)
   - Criar novo environment
   - Ativá-lo antes de executar a collection

**✅ Versão Atual:** Usa `pm.collectionVariables` - **funciona sem configuração adicional!**

---

### **Problema: 403 Forbidden em /me ou /logout**

**Causa:** Sessão não existe ou expirou

**Solução:**
1. Execute `3. Login` primeiro
2. Verifique que cookie `JSESSIONID` foi criado:
   - Ver em `Cookies` (abaixo da URL)
3. Execute `/me` ou `/logout` imediatamente após login

---

### **Problema: 400 Username já existe**

**Causa:** Username gerado já existe no banco

**Solução:**
1. O script já gera username único automaticamente
2. Se ainda ocorrer, reinicie a aplicação (limpa banco H2 em memória)

---

### **Problema: 401 Credenciais inválidas no login**

**Causa:** Senha incorreta ou usuário não existe

**Solução:**
1. Verifique que executou `2. Register` antes
2. Confira que senha é `Test@1234` (case-sensitive)
3. Verifique que variável `{{testUsername}}` está definida

---

### **Problema: Connection refused**

**Causa:** Aplicação não está rodando

**Solução:**
```bash
# Executar aplicação
./mvnw spring-boot:run

# Aguardar até ver:
# "Started NeuroefficiencyApplication in X seconds"

# Testar:
curl http://localhost:8082/api/auth/health
```

---

## 🎓 DICAS PRO

### **1. Executar Collection Inteira**
- Clicar com botão direito na collection
- Selecionar `Run collection`
- Ajustar ordem de execução se necessário
- Clicar `Run`
- **Resultado:** Todos os endpoints executados em sequência

### **2. Salvar Respostas como Exemplos**
- Após executar endpoint
- Clicar em `Save Response`
- Clicar em `Save as Example`
- **Benefício:** Documentação visual de respostas esperadas

### **3. Exportar Environment**
- Ir em `Environments`
- Selecionar environment usado
- Clicar nos `...` → `Export`
- **Benefício:** Compartilhar configurações com equipe

### **4. Ver Histórico de Requisições**
- Ir em `History` (painel esquerdo)
- Ver todas as requisições executadas
- **Benefício:** Revisar execuções anteriores

### **5. Usar Pre-request Scripts**
- Alguns endpoints já têm scripts pre-request
- Exemplo: `2. Register` gera username automático
- **Customizar:** Editar script em aba `Pre-request Script`

---

## 📈 MÉTRICAS DE QUALIDADE

### **Collection Completa:**
- ✅ 8 endpoints (5 funcionais + 3 validações)
- ✅ Testes automatizados em todos os endpoints
- ✅ Username único automático
- ✅ Gerenciamento de sessão automático
- ✅ Variáveis de ambiente
- ✅ Logs no console
- ✅ Scripts pre-request e post-request
- ✅ Descrições detalhadas

### **Cobertura de Testes:**
- ✅ Status codes (100%)
- ✅ Estrutura de resposta (100%)
- ✅ Validação de dados (100%)
- ✅ Presença de cookies (100%)
- ✅ Erros esperados (100%)

---

## 🎉 CONCLUSÃO

**Collection pronta para uso!**

- ✅ 100% funcional e testada
- ✅ Automação completa
- ✅ Fácil de usar
- ✅ Bem documentada
- ✅ Testes automatizados

**Comece agora:**
1. Importar collection
2. Executar `1. Health Check`
3. Executar endpoints na ordem
4. Verificar testes passando ✅

---

**🚀 Boa testagem!**

