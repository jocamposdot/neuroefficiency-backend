import json

with open('Neuroefficiency_Auth.postman_collection.json', 'r', encoding='utf-8') as f:
    collection = json.load(f)

print("Corrigindo endpoint 'Username Duplicado'...\n")

for item in collection['item']:
    if 'Username Duplicado' in item['name'] or 'Duplicado' in item['name']:
        print(f"Endpoint encontrado: {item['name']}")
        
        # Atualizar pre-request script para REUSAR o username
        if 'event' in item:
            for event in item['event']:
                if event.get('listen') == 'prerequest':
                    # Substituir o script para apenas reusar o username existente
                    event['script']['exec'] = [
                        "// Reusar o mesmo username do endpoint anterior (Register)",
                        "// Este endpoint testa username duplicado, então DEVE usar o username já registrado",
                        "var username = pm.collectionVariables.get(\"testUsername\");",
                        "console.log(\"📝 Reusando username: \" + username + \" (deve falhar por duplicação)\");"
                    ]
                    print(f"✅ Pre-request script corrigido - agora reusa o username")
                    break

# Salvar
with open('Neuroefficiency_Auth.postman_collection.json', 'w', encoding='utf-8') as f:
    json.dump(collection, f, indent=4, ensure_ascii=False)

print(f"\n{'='*60}")
print(f"✅ Correção aplicada!")
print(f"{'='*60}")
print("\nAgora o endpoint 'Username Duplicado' irá:")
print("1. Reusar o username do endpoint 2 (Register)")
print("2. Tentar registrar novamente com mesmo username")
print("3. Deve retornar 400 ou 409 (username já existe)")

