-- Script para atribuir role ADMIN ao usuário criado
-- Execute este script no H2 Console: http://localhost:8082/h2-console

-- 1. Verificar usuários existentes
SELECT id, username, email FROM usuarios;

-- 2. Verificar roles existentes
SELECT id, name, description FROM roles;

-- 3. Atribuir role ADMIN ao usuário admin_test (ou o primeiro usuário)
INSERT INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
FROM usuarios u, roles r
WHERE u.username = 'admin_test' AND r.name = 'ADMIN';

-- 4. Verificar se a atribuição foi feita
SELECT u.username, r.name as role_name
FROM usuarios u
JOIN usuario_roles ur ON u.id = ur.usuario_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin_test';

-- 5. Verificar permissões do usuário ADMIN
SELECT u.username, r.name as role_name, p.name as permission_name
FROM usuarios u
JOIN usuario_roles ur ON u.id = ur.usuario_id
JOIN roles r ON ur.role_id = r.id
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE u.username = 'admin_test';
