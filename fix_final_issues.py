import json

with open('Neuroefficiency_Auth.postman_collection.json', 'r', encoding='utf-8') as f:
    collection = json.load(f)

print("Corrigindo últimos problemas...\n")

count = 0

for item in collection['item']:
    # 1. Corrigir Password Reset Request para usar {{testUsername}}
    if 'Password Reset - Request' in item['name']:
        if 'request' in item and 'body' in item['request']:
            body = item['request']['body']
            if 'raw' in body and '{{username}}' in body['raw']:
                body['raw'] = body['raw'].replace(
                    '"email": "{{username}}@example.com"',
                    '"email": "{{testUsername}}@example.com"'
                )
                print(f"✅ {item['name']}: Corrigido para {{{{testUsername}}}}")
                count += 1
    
    # 2. Adicionar pre-request script no endpoint de validação Username Duplicado
    if 'Username Duplicado' in item['name'] or 'Duplicado' in item['name']:
        # Verificar se já tem pre-request script
        has_prerequest = False
        if 'event' in item:
            for event in item['event']:
                if event.get('listen') == 'prerequest':
                    has_prerequest = True
                    break
        
        # Se não tem, adicionar
        if not has_prerequest:
            if 'event' not in item:
                item['event'] = []
            
            item['event'].append({
                "listen": "prerequest",
                "script": {
                    "exec": [
                        "// Usar o mesmo username do endpoint anterior (Register)",
                        "// Este endpoint testa username duplicado, então deve usar o username já registrado",
                        "var username = pm.collectionVariables.get(\"testUsername\");",
                        "if (!username) {",
                        "    // Se não existe, gerar um novo",
                        "    var timestamp = new Date().getTime();",
                        "    username = \"testuser\" + timestamp;",
                        "    pm.collectionVariables.set(\"testUsername\", username);",
                        "}",
                        "console.log(\"📝 Usando username: \" + username + \" (deve estar duplicado)\");"
                    ],
                    "type": "text/javascript"
                }
            })
            print(f"✅ {item['name']}: Pre-request script adicionado")
            count += 1

# Salvar
with open('Neuroefficiency_Auth.postman_collection.json', 'w', encoding='utf-8') as f:
    json.dump(collection, f, indent=4, ensure_ascii=False)

print(f"\n{'='*60}")
print(f"✅ Correções finais aplicadas!")
print(f"📝 {count} endpoints corrigidos")
print(f"{'='*60}")

