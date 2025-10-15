import json

with open('Neuroefficiency_Auth.postman_collection.json', 'r', encoding='utf-8') as f:
    collection = json.load(f)

print("Corrigindo variáveis de username...\n")

count = 0
for item in collection['item']:
    if 'request' in item and 'body' in item['request']:
        body = item['request']['body']
        if 'raw' in body:
            raw = body['raw']
            
            # Substituir {{username}} por {{testUsername}} onde necessário
            if '"username": "{{username}}"' in raw:
                new_raw = raw.replace(
                    '"username": "{{username}}"',
                    '"username": "{{testUsername}}"'
                )
                
                # Manter o email como {{testUsername}} também
                new_raw = new_raw.replace(
                    '"email": "{{username}}@example.com"',
                    '"email": "{{testUsername}}@example.com"'
                )
                
                body['raw'] = new_raw
                print(f"✅ {item['name']}: Variáveis corrigidas")
                count += 1

# Salvar
with open('Neuroefficiency_Auth.postman_collection.json', 'w', encoding='utf-8') as f:
    json.dump(collection, f, indent=4, ensure_ascii=False)

print(f"\n{'='*60}")
print(f"✅ Collection corrigida!")
print(f"📝 {count} endpoints atualizados")
print(f"{'='*60}")

# Verificar
print("\nVerificação:")
for item in collection['item']:
    if 'Register' in item['name'] or 'Username Duplicado' in item['name']:
        if 'body' in item.get('request', {}):
            body = item['request']['body'].get('raw', '')
            if 'testUsername' in body:
                print(f"✅ {item['name']}: Usa {{{{testUsername}}}}")
            elif '{{username}}' in body:
                print(f"❌ {item['name']}: Ainda usa {{{{username}}}}")

