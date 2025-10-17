# 🧪 Scripts de Teste - Neuroefficiency

Esta pasta contém todos os scripts de teste organizados por funcionalidade.

## 📁 Estrutura Organizada (Pós-Limpeza)

```
scripts/testes/
├── rbac/                    # Testes RBAC (Fase 3) - 7 scripts essenciais
│   ├── GUIA-TESTE-RBAC.md   # Guia completo de testes RBAC
│   ├── setup-admin-user.sql # SQL para configurar usuário ADMIN
│   ├── setup-admin.ps1      # Script de setup de admin
│   ├── test-endpoints-final.ps1    # Teste final de endpoints
│   ├── test-rbac-complete.ps1      # Teste completo RBAC
│   ├── test-rbac-endpoints.ps1     # Teste de endpoints específicos
│   └── test-rbac-simple.ps1        # Teste simples RBAC
├── auth/                    # Testes de Autenticação (Fases 1-2) - 7 scripts essenciais
│   ├── test-api.ps1         # Teste básico de API
│   ├── test-complete-auto.ps1      # Teste automatizado completo
│   ├── test-complete-flow.ps1      # Fluxo completo de autenticação
│   ├── test-fresh.ps1       # Teste do zero
│   ├── test-full-e2e.ps1    # Teste end-to-end
│   ├── test-password-reset.ps1     # Teste específico de recuperação
│   └── test-simple.ps1      # Teste simples e direto
└── utilitarios/             # Scripts utilitários - 4 scripts
    ├── get-token.ps1        # Obter token de autenticação
    ├── debug-email.ps1      # Debug de email
    ├── check-token-db.ps1   # Verificar token no banco
    └── check-rate-limit.ps1 # Verificar rate limiting
```

## 🚀 Como Usar

### **Testes RBAC (Fase 3)**
```powershell
# Teste simples RBAC
cd scripts/testes/rbac
powershell -ExecutionPolicy Bypass -File test-rbac-simple.ps1

# Teste completo RBAC
powershell -ExecutionPolicy Bypass -File test-rbac-complete.ps1

# Teste final de endpoints
powershell -ExecutionPolicy Bypass -File test-endpoints-final.ps1
```

### **Testes de Autenticação (Fases 1-2)**
```powershell
# Teste básico
cd scripts/testes/auth
powershell -ExecutionPolicy Bypass -File test-simple.ps1

# Teste completo
powershell -ExecutionPolicy Bypass -File test-complete-flow.ps1

# Teste end-to-end
powershell -ExecutionPolicy Bypass -File test-full-e2e.ps1
```

### **Scripts Utilitários**
```powershell
# Obter token
cd scripts/testes/utilitarios
powershell -ExecutionPolicy Bypass -File get-token.ps1

# Verificar rate limit
powershell -ExecutionPolicy Bypass -File check-rate-limit.ps1
```

## 📋 Pré-requisitos

1. **API rodando:** http://localhost:8082
2. **PowerShell:** Versão 5.1 ou superior
3. **Execution Policy:** Bypass (temporário)

## 🔧 Configuração

Para executar os scripts, use:
```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
```

## 📚 Documentação

- **Guia RBAC:** `rbac/GUIA-TESTE-RBAC.md`
- **Documentação Principal:** `../../README.md`
- **Princípios Arquiteturais:** `../../DOCS/PRINCIPIOS-ARQUITETURAIS.md`

## 🎯 Status dos Testes

- ✅ **Fase 1 - Autenticação:** Testes completos
- ✅ **Fase 2 - Reset de Senha:** Testes completos  
- ✅ **Fase 3 - RBAC:** Testes completos

---

## 📊 **Resumo da Organização**

- ✅ **Scripts organizados** em pastas por funcionalidade
- ✅ **Redundâncias removidas** (9 scripts duplicados)
- ✅ **Estrutura limpa** (27 → 18 scripts essenciais)
- ✅ **Documentação atualizada** com comandos corretos

---

**Última atualização:** 16 de Outubro de 2025  
**Versão:** 3.1 - Scripts Organizados e Otimizados  
**Redundância:** 0% (todos os scripts têm propósito específico)
