-- ===================================================================
-- Migração V2: Adicionar campo email à tabela usuarios
-- Data: 2025-10-14
-- Descrição: Adiciona email para recuperação de senha (Tarefa 2)
-- Estratégia: Email opcional para usuários legacy da Fase 1
-- ===================================================================

-- Adicionar coluna email (NULLABLE para não quebrar registros existentes)
ALTER TABLE usuarios ADD COLUMN email VARCHAR(255);

-- Criar índice único
-- NOTA: H2 não suporta partial indexes (WHERE clause)
-- H2 permite múltiplos NULL em UNIQUE INDEX automaticamente
-- Compatível com H2 e PostgreSQL
CREATE UNIQUE INDEX uk_usuarios_email ON usuarios(email);

-- Comentário descritivo (H2 2.x suporta COMMENT)
COMMENT ON COLUMN usuarios.email IS 'Email do usuário (obrigatório para novos registros, opcional para usuários legacy da Fase 1)';

