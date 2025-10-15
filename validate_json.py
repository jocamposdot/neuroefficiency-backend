import json

try:
    with open('Neuroefficiency_Auth.postman_collection.json', 'r', encoding='utf-8') as f:
        collection = json.load(f)
    
    print("="*60)
    print("✅ JSON VÁLIDO!")
    print("="*60)
    print(f"\n📊 Total de endpoints: {len(collection['item'])}")
    print(f"📝 Nome: {collection['info']['name']}")
    print(f"🆔 ID: {collection['info']['_postman_id']}")
    
    print("\n📋 Endpoints com campo email no body:")
    print("-"*60)
    
    for i, item in enumerate(collection['item'], 1):
        if 'body' in item.get('request', {}):
            body = item['request']['body'].get('raw', '')
            if 'username' in body:
                has_email = '"email"' in body
                status = "✅" if has_email else "❌"
                print(f"{status} {i}. {item['name']}")
    
    print("\n" + "="*60)
    print("PRONTO PARA TESTAR NO POSTMAN!")
    print("="*60)
    
except json.JSONDecodeError as e:
    print(f"❌ ERRO NO JSON: {e}")
except Exception as e:
    print(f"❌ ERRO: {e}")

