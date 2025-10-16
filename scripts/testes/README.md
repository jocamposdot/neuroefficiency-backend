# 🧪 Scripts de Teste - Neuroefficiency

Esta pasta contém todos os scripts de teste organizados por funcionalidade.

## 📁 Estrutura

```
scripts/testes/
├── rbac/                    # Testes RBAC (Fase 3)
│   ├── GUIA-TESTE-RBAC.md   # Guia completo de testes RBAC
│   ├── setup-admin-user.sql # SQL para configurar usuário ADMIN
│   ├── setup-admin.ps1      # Script de setup de admin
│   ├── test-rbac-*.ps1      # Scripts de teste RBAC
│   └── teste-*-rbac.ps1     # Scripts de teste RBAC (português)
├── auth/                    # Testes de Autenticação (Fases 1-2)
│   ├── test-simple.ps1      # Teste básico de autenticação
│   ├── test-complete-*.ps1  # Testes completos
│   ├── test-password-reset.ps1 # Testes de reset de senha
│   └── test-api.ps1         # Testes gerais da API
└── utilitarios/             # Scripts utilitários
    ├── get-token.ps1        # Obter token de autenticação
    ├── debug-email.ps1      # Debug de email
    ├── check-token-db.ps1   # Verificar token no banco
    └── check-rate-limit.ps1 # Verificar rate limiting
```

## 🚀 Como Usar

### **Testes RBAC (Fase 3)**
```powershell
# Teste completo RBAC
cd scripts/testes/rbac
powershell -ExecutionPolicy Bypass -File teste-completo-rbac.ps1

# Teste final RBAC
powershell -ExecutionPolicy Bypass -File teste-final-rbac.ps1
```

### **Testes de Autenticação (Fases 1-2)**
```powershell
# Teste básico
cd scripts/testes/auth
powershell -ExecutionPolicy Bypass -File test-simple.ps1

# Teste completo
powershell -ExecutionPolicy Bypass -File test-complete-flow.ps1
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

**Última atualização:** 2025-10-16
**Versão:** 3.0 - RBAC Implementado
