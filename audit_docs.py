"""
Script para auditar documentação e identificar inconsistências
relacionadas à Collection Postman v2.0
"""

import os
import json
import re

# Carregar collection para validar
with open('Neuroefficiency_Auth.postman_collection.json', 'r', encoding='utf-8') as f:
    collection = json.load(f)

collection_version = collection['info']['name']
collection_id = collection['info']['_postman_id']
total_endpoints = len(collection['item'])

print("="*70)
print("🔍 AUDITORIA DE DOCUMENTAÇÃO - COLLECTION POSTMAN")
print("="*70)
print(f"\n📦 COLLECTION ATUAL:")
print(f"   Nome: {collection_version}")
print(f"   ID: {collection_id}")
print(f"   Total Endpoints: {total_endpoints}")
print(f"\n   Endpoints:")
for i, item in enumerate(collection['item'], 1):
    print(f"   {i:2d}. {item['name']}")

# Arquivos para auditar
docs_to_check = [
    'README.md',
    'DOCS/GUIA_POSTMAN.md',
    'DOCS/GUIA_TÉCNICO_COMPLETO.md',
    'DOCS/TAREFA-2-REFERENCIA.md',
    'DOCS/CHANGELOG.md',
    'DOCS/GUIA_TESTES.md',
    'DOCS/GUIA_SETUP_DESENVOLVIMENTO.md',
]

# Padrões a procurar
patterns = {
    'collection_version': [
        r'v1\.1',
        r'versão 1\.1',
        r'collection v1\.1',
        r'Versão.*?1\.1',
    ],
    'endpoint_count': [
        r'9/9 endpoints',
        r'9 endpoints',
        r'8 endpoints',
        r'8/8',
    ],
    'old_collection_id': [
        r'neuro-auth-v1',
        r'postman_collection_id.*?neuro-auth-v1',
    ]
}

print(f"\n{'='*70}")
print("📄 ANÁLISE DE DOCUMENTOS")
print("="*70)

issues_found = []

for doc_path in docs_to_check:
    if not os.path.exists(doc_path):
        print(f"\n⚠️  {doc_path}")
        print(f"   Arquivo não encontrado!")
        continue
    
    print(f"\n📖 {doc_path}")
    
    with open(doc_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    doc_issues = []
    
    # Verificar versão da collection
    for pattern in patterns['collection_version']:
        matches = re.finditer(pattern, content, re.IGNORECASE)
        for match in matches:
            line_num = content[:match.start()].count('\n') + 1
            doc_issues.append({
                'type': 'version',
                'line': line_num,
                'match': match.group(),
                'should_be': 'v2.0 ou 2.0'
            })
    
    # Verificar contagem de endpoints
    for pattern in patterns['endpoint_count']:
        matches = re.finditer(pattern, content, re.IGNORECASE)
        for match in matches:
            line_num = content[:match.start()].count('\n') + 1
            doc_issues.append({
                'type': 'count',
                'line': line_num,
                'match': match.group(),
                'should_be': f'{total_endpoints} endpoints'
            })
    
    # Verificar ID antigo da collection
    for pattern in patterns['old_collection_id']:
        matches = re.finditer(pattern, content, re.IGNORECASE)
        for match in matches:
            line_num = content[:match.start()].count('\n') + 1
            doc_issues.append({
                'type': 'id',
                'line': line_num,
                'match': match.group(),
                'should_be': collection_id
            })
    
    if doc_issues:
        print(f"   ❌ {len(doc_issues)} problema(s) encontrado(s):")
        for issue in doc_issues:
            print(f"      Linha {issue['line']}: '{issue['match']}' → '{issue['should_be']}'")
        issues_found.append({'doc': doc_path, 'issues': doc_issues})
    else:
        print(f"   ✅ Documento OK")

# Resumo
print(f"\n{'='*70}")
print("📊 RESUMO DA AUDITORIA")
print("="*70)

if issues_found:
    print(f"\n❌ {len(issues_found)} documento(s) com problemas:")
    total_issues = sum(len(doc['issues']) for doc in issues_found)
    print(f"   Total de inconsistências: {total_issues}")
    print(f"\n📝 DOCUMENTOS QUE PRECISAM ATUALIZAÇÃO:")
    for doc in issues_found:
        print(f"   • {doc['doc']} ({len(doc['issues'])} problema(s))")
else:
    print(f"\n✅ Todos os documentos estão atualizados!")

# Verificar redundância
print(f"\n{'='*70}")
print("🔍 VERIFICAÇÃO DE REDUNDÂNCIA")
print("="*70)

redundancy_check = {
    'README.md': ['Tarefa 2', 'Recuperação', 'Password Reset'],
    'DOCS/GUIA_POSTMAN.md': ['Collection', 'Postman', 'Endpoints'],
    'DOCS/TAREFA-2-REFERENCIA.md': ['Tarefa 2', 'Password Reset', 'Email'],
    'DOCS/GUIA_TESTES.md': ['Testes', 'Manual', 'E2E'],
}

print("\n📄 Análise de conteúdo por documento:")
for doc_path, keywords in redundancy_check.items():
    if os.path.exists(doc_path):
        with open(doc_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        word_count = len(content.split())
        line_count = len(content.split('\n'))
        keyword_matches = sum(content.lower().count(kw.lower()) for kw in keywords)
        
        print(f"\n   {doc_path}")
        print(f"   • Linhas: {line_count}")
        print(f"   • Palavras: {word_count:,}")
        print(f"   • Menções de keywords: {keyword_matches}")

print(f"\n{'='*70}")
print("✅ AUDITORIA COMPLETA")
print("="*70)

