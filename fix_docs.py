"""
Script para corrigir automaticamente as inconsistências na documentação
"""

import re

fixes = {
    'README.md': [
        # Versão da collection
        (r'Collection:.*?v1\.1', 'Collection:** `Neuroefficiency_Auth.postman_collection.json` (v2.0)', re.IGNORECASE),
        (r'collection v1\.1', 'collection v2.0', re.IGNORECASE),
        (r'Collection Postman v1\.1', 'Collection Postman v2.0', re.IGNORECASE),
        # Contagem de endpoints
        (r'(\*\*Endpoints\*\*\s*\|\s*)9/9', r'\g<1>12/12', 0),
        (r'(\*\*\|\s*)9 endpoints', r'\g<1>12 endpoints', 0),
        (r'9/9 endpoints operacionais', '12/12 endpoints operacionais', 0),
        (r'9/9 endpoints', '12/12 endpoints', 0),
        (r'(\d+)\s+endpoints\s+documentados\s+\(5 funcionais \+ 3 validações\)', '12 endpoints documentados (5 Fase 1 + 4 Fase 2 + 3 validações)', re.IGNORECASE),
        (r'8 endpoints documentados', '12 endpoints documentados', re.IGNORECASE),
    ],
    'DOCS/GUIA_POSTMAN.md': [
        # Linha 5 e linha 837
        (r'(\*\*Endpoints:\*\*\s*)9/9\s+\(5 Auth \+ 4 Password Reset\)', r'\g<1>12/12 (5 Auth + 4 Password Reset + 3 Validações)', 0),
        (r'9 endpoints funcionando', '12 endpoints funcionando', 0),
    ],
    'DOCS/GUIA_TÉCNICO_COMPLETO.md': [
        (r'Collection Postman v1\.1', 'Collection Postman v2.0', re.IGNORECASE),
        (r'v1\.1', 'v2.0', 0),
    ],
    'DOCS/CHANGELOG.md': [
        (r'Collection Postman v1\.1', 'Collection Postman v2.0', re.IGNORECASE),
        (r'v1\.1', 'v2.0', 0),
    ],
    'DOCS/GUIA_TESTES.md': [
        (r'(\*\*Endpoints:\*\*\s*)9', r'\g<1>12', 0),
        (r'9 endpoints', '12 endpoints', 0),
        (r'9/9', '12/12', 0),
    ],
}

print("="*70)
print("🔧 CORREÇÃO AUTOMÁTICA DE DOCUMENTAÇÃO")
print("="*70)

for file_path, replacements in fixes.items():
    print(f"\n📝 Processando: {file_path}")
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        changes_made = 0
        
        for pattern, replacement, flags in replacements:
            new_content = re.sub(pattern, replacement, content, flags=flags)
            if new_content != content:
                changes_made += 1
                content = new_content
        
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"   ✅ {changes_made} correção(ões) aplicada(s)")
        else:
            print(f"   ℹ️  Nenhuma alteração necessária")
    
    except FileNotFoundError:
        print(f"   ⚠️  Arquivo não encontrado")
    except Exception as e:
        print(f"   ❌ Erro: {e}")

print(f"\n{'='*70}")
print("✅ CORREÇÕES APLICADAS COM SUCESSO")
print("="*70)
print("\nResumo:")
print("• Versão da collection: v1.1 → v2.0")
print("• Contagem de endpoints: 8/9 → 12/12")
print("• Documentação atualizada e consistente")

