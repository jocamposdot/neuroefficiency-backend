-- ===================================================================
-- Migração V1: Criação da tabela de usuários
-- Data: 2025-10-11
-- Descrição: Tabela base para autenticação e gestão de usuários
-- ===================================================================

CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3)
);

-- Índices para performance
CREATE INDEX idx_usuarios_username ON usuarios(username);
CREATE INDEX idx_usuarios_enabled ON usuarios(enabled);
CREATE INDEX idx_usuarios_created_at ON usuarios(created_at);

-- Comentários descritivos
COMMENT ON TABLE usuarios IS 'Tabela principal de usuários do sistema Neuroefficiency';
COMMENT ON COLUMN usuarios.id IS 'Identificador único do usuário';
COMMENT ON COLUMN usuarios.username IS 'Nome de usuário único (3-50 caracteres)';
COMMENT ON COLUMN usuarios.password_hash IS 'Hash BCrypt da senha do usuário';
COMMENT ON COLUMN usuarios.enabled IS 'Indica se a conta está ativa';
COMMENT ON COLUMN usuarios.account_non_expired IS 'Indica se a conta não expirou';
COMMENT ON COLUMN usuarios.account_non_locked IS 'Indica se a conta não está bloqueada';
COMMENT ON COLUMN usuarios.credentials_non_expired IS 'Indica se as credenciais não expiraram';
COMMENT ON COLUMN usuarios.created_at IS 'Data e hora de criação do usuário';
COMMENT ON COLUMN usuarios.updated_at IS 'Data e hora da última atualização';

