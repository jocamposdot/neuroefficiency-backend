-- ===================================================================
-- Migração V2: Adicionar campo email à tabela usuarios
-- Data: 2025-10-14
-- Descrição: Adiciona email para recuperação de senha (Tarefa 2)
-- Estratégia: Email opcional para usuários legacy da Fase 1
-- ===================================================================

-- Adicionar coluna email (NULLABLE temporariamente para não quebrar registros existentes)
ALTER TABLE usuarios ADD COLUMN email VARCHAR(255);

-- Criar índice parcial único (só para emails não-nulos)
-- Permite que usuários antigos não tenham email, mas novos devem ter
CREATE UNIQUE INDEX uk_usuarios_email ON usuarios(email) WHERE email IS NOT NULL;

-- Criar índice para performance em buscas
CREATE INDEX idx_usuarios_email ON usuarios(email);

-- Comentário descritivo
COMMENT ON COLUMN usuarios.email IS 'Email do usuário (obrigatório para novos registros, opcional para usuários legacy da Fase 1)';

