# 🚀 GUIA RÁPIDO - Collection Postman 100% Funcional

**Versão:** 3.0  
**Última Atualização:** 17 de Outubro de 2025  
**Tempo de Setup:** ~5 minutos

---

## 📋 **PRÉ-REQUISITOS**

✅ Java 21 instalado  
✅ Postman instalado  
✅ Navegador web (para H2 Console)  
⚠️ Docker instalado (OPCIONAL - apenas para Fase 2)

---

## 🎯 **SETUP COMPLETO EM 3 PASSOS**

### **🔹 PASSO 1: Iniciar a Aplicação**

**No terminal, na raiz do projeto:**

```bash
# Windows PowerShell
./mvnw spring-boot:run

# Aguardar mensagem:
# "Started NeuroefficiencyApplication in X seconds"
```

**✅ Verificar:** Abrir `http://localhost:8082/api/auth/health`  
**Resposta esperada:** `{"status":"UP","service":"Authentication Service","version":"1.0"}`

---

### **🔹 PASSO 2: Importar Collection no Postman**

1. Abrir Postman
2. Clicar em `File` → `Import` (ou `Ctrl+O`)
3. Selecionar: `Neuroefficiency_Auth_v3.postman_collection.json`
4. ✅ Collection importada com sucesso!

---

### **🔹 PASSO 3: Executar Endpoints 1-10 (Setup Básico)**

**No Postman, executar NA ORDEM:**

1. ✅ **Endpoint 1:** Health Check → 200 OK
2. ✅ **Endpoint 2:** Register → 201 Created (cria usuário teste)
3. ✅ **Endpoint 3:** Login → 200 OK (autentica usuário teste)
4. ✅ **Endpoint 4:** Me → 200 OK (dados do usuário)
5. ✅ **Endpoint 5:** Logout → 200 OK
6. ⚠️ **Endpoints 6-9:** Password Reset (OPCIONAL - requer MailHog)
7. ✅ **Endpoint 10:** Create Admin User → 201 Created

**⚠️ ATENÇÃO:** Após executar endpoint 10, você verá no console do Postman:

```javascript
'⚠️ ATENÇÃO: Atribua role ADMIN via H2 Console:'
'   INSERT INTO usuario_roles (usuario_id, role_id)'
'   VALUES (X, (SELECT id FROM roles WHERE name=\'ADMIN\'));'
```

**📝 COPIAR ESTE SQL!** Você vai precisar dele no próximo passo.

---

## 🔑 **PASSO CRÍTICO: ATRIBUIR ROLE ADMIN**

### **🔹 PASSO 4: Acessar H2 Console**

1. **Abrir navegador:** `http://localhost:8082/h2-console`

2. **Configurar conexão:**
   ```
   JDBC URL: jdbc:h2:mem:neurodb
   Username: sa
   Password: (deixar vazio)
   ```

3. **Clicar em:** `Connect`

---

### **🔹 PASSO 5: Executar SQL (COPIE DO CONSOLE DO POSTMAN)**

**No H2 Console, colar o SQL que você copiou do Postman:**

```sql
INSERT INTO usuario_roles (usuario_id, role_id)
VALUES (2, (SELECT id FROM roles WHERE name='ADMIN'));
```

**✅ Clicar em:** `Run` (ou pressionar `Ctrl+Enter`)

**Resultado esperado:**
```
(1 row, X ms)
```

✅ **Role ADMIN atribuída com sucesso!**

---

### **🔹 PASSO 6: Continuar Testando no Postman**

**Executar endpoints 11-27:**

11. ✅ **Login Admin** → 200 OK (autentica como ADMIN)
12. ✅ **List Roles** → 200 OK (agora funciona!)
13. ✅ **Create Role** → 200 OK
14. ✅ **List Permissions** → 200 OK
15-25. ✅ **Demais endpoints RBAC** → Todos 200 OK!
26-27. ✅ **Validações** → 403/409 (comportamentos esperados)

---

## 🎉 **RESULTADO FINAL**

```
✅ Fase 1 - Autenticação: 5/5 (100%)
✅ Fase 2 - Password Reset: 3/4 (75%)* 
✅ Fase 3 - RBAC: 16/16 (100%)
✅ Validações: 2/2 (100%)
═══════════════════════════════════
TOTAL: 26/27 (96%) ✅

* Endpoint 6 requer MailHog (opcional)
```

---

## 📊 **TROUBLESHOOTING RÁPIDO**

### **Problema: 403 Forbidden nos endpoints RBAC**

**Causa:** Role ADMIN não foi atribuída

**Solução:** Executar o PASSO 5 novamente (SQL no H2 Console)

**Verificar:** Execute este SQL no H2 Console:
```sql
SELECT u.id, u.username, r.name as role_name
FROM usuarios u
LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username LIKE 'admin%';
```

**Resultado esperado:**
```
ID | USERNAME            | ROLE_NAME
2  | admin1760663556211 | ADMIN
```

---

### **Problema: 500 Internal Server Error no endpoint 6**

**Causa:** MailHog não está rodando

**Solução (OPCIONAL):**
```bash
# Docker
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog

# Verificar
# Abrir: http://localhost:8025
```

**OU:** Pule os endpoints 6-9 (Fase 2 é opcional)

---

### **Problema: Porta 8082 já está em uso**

**Causa:** Outra instância da aplicação rodando

**Solução:**
```powershell
# Windows
taskkill /F /IM java.exe

# Reiniciar aplicação
./mvnw spring-boot:run
```

---

## 🔄 **FLUXO COMPLETO - REPETIR SEMPRE**

### **Toda vez que quiser testar:**

```
1. Iniciar aplicação → ./mvnw spring-boot:run
2. Aguardar 15-20 segundos
3. Importar collection (se ainda não importou)
4. Executar endpoints 1-10 no Postman
5. Copiar SQL do console (endpoint 10)
6. Abrir H2 Console → http://localhost:8082/h2-console
7. Executar SQL copiado
8. Continuar executando endpoints 11-27
9. ✅ Sucesso! 26/27 endpoints funcionando
```

---

## 🎯 **SETUP OPCIONAL - FASE 2 (PASSWORD RESET)**

### **Se quiser testar recuperação de senha:**

**Passo 1: Iniciar MailHog**
```bash
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

**Passo 2: Executar endpoint 6 no Postman**
- POST /api/auth/password-reset/request
- Resultado: 200 OK

**Passo 3: Abrir MailHog**
- URL: `http://localhost:8025`
- Ver email recebido
- Copiar token (64 caracteres)

**Passo 4: Executar endpoint 7**
- GET /api/auth/password-reset/validate-token/COLE_TOKEN_AQUI
- Substituir "COLE_TOKEN_AQUI" pelo token copiado
- Resultado: 200 OK (valid: true)

**Passo 5: Executar endpoint 8**
- POST /api/auth/password-reset/confirm
- Usar token copiado no body
- Resultado: 200 OK

---

## 📝 **CHECKLIST RÁPIDO**

Antes de executar collection:

- [ ] Aplicação rodando? (`http://localhost:8082/api/auth/health`)
- [ ] Collection importada no Postman?
- [ ] Executei endpoints 1-10?
- [ ] Copiei SQL do console?
- [ ] Executei SQL no H2 Console?
- [ ] Executei endpoint 11 (Login Admin)?
- [ ] ✅ Pronto para testar endpoints 12-27!

---

## 🚀 **SCRIPTS AUTOMATIZADOS (OPCIONAL)**

### **Script PowerShell - Setup Completo**

Criamos um script para automatizar tudo:

```powershell
# Ver: scripts/testes/rbac/setup-admin.ps1

# Uso:
./scripts/testes/rbac/setup-admin.ps1

# O script faz:
# 1. Cria usuário admin
# 2. Mostra SQL para copiar
# 3. Espera você executar no H2
# 4. Faz login admin
# 5. Testa endpoints RBAC
```

---

## 📖 **DOCUMENTAÇÃO ADICIONAL**

- **Guia Completo:** `DOCS/GUIA_POSTMAN.md` (1.450 linhas)
- **Análise de Gaps:** `DOCS/ANALISE-GAPS-COLLECTION-V3.md`
- **Validação Fase 3:** `DOCS/VALIDACAO-COMPLETA-FASE-3.md`
- **Collection JSON:** `Neuroefficiency_Auth_v3.postman_collection.json`

---

## 💡 **DICAS IMPORTANTES**

### **✅ DOs:**
- ✅ Executar endpoints NA ORDEM (1 → 27)
- ✅ Aguardar aplicação iniciar completamente
- ✅ Verificar logs do console Postman
- ✅ Salvar SQL do endpoint 10 para reusar

### **❌ DON'Ts:**
- ❌ Pular o endpoint 10 (Create Admin User)
- ❌ Esquecer de executar SQL no H2 Console
- ❌ Tentar acessar RBAC sem role ADMIN
- ❌ Executar fora de ordem

---

## 🎯 **RESUMO EXECUTIVO**

### **Por que preciso executar SQL manualmente?**

O H2 Console **não tem API REST**. A única forma de atribuir roles é:
1. Via SQL direto no H2 Console (desenvolvimento)
2. Via migration Flyway (produção)

A collection **não pode** executar SQL automaticamente porque:
- ❌ Postman scripts não têm acesso JDBC
- ❌ H2 Console não expõe API REST
- ❌ Seria necessário biblioteca Java (impossível em JavaScript)

**Mas:** É apenas **1 linha de SQL, 1 vez por sessão**. Muito simples!

---

## 📊 **TEMPO ESTIMADO**

### **Primeira Vez:**
- Iniciar aplicação: 20 segundos
- Importar collection: 10 segundos
- Executar setup (endpoints 1-10): 30 segundos
- Atribuir role ADMIN (H2): 30 segundos
- Testar RBAC (endpoints 11-27): 60 segundos
**TOTAL: ~2-3 minutos**

### **Próximas Vezes:**
- Aplicação já rodando: 0 segundos
- Collection já importada: 0 segundos
- Setup rápido: 30 segundos
- Atribuir role: 20 segundos (SQL já copiado)
- Testar: 60 segundos
**TOTAL: ~2 minutos**

---

## 🎉 **PRONTO!**

Seguindo este guia, você terá **100% da collection funcionando** sempre que precisar testar!

**Dúvidas?** Consulte `DOCS/GUIA_POSTMAN.md` para documentação completa.

---

**Última atualização:** 17 de Outubro de 2025  
**Versão Collection:** 3.0  
**Status:** ✅ Testado e Funcional

