-- ===================================================================
-- Migration V6: Create Audit Logs Table
-- 
-- Fase 4: Audit Logging Avançado
-- 
-- Cria a tabela de logs de auditoria para rastrear todas as ações
-- críticas no sistema Neuroefficiency.
-- 
-- Autor: Neuroefficiency Team
-- Data: 2025-11-12
-- Versão: 4.0
-- ===================================================================

-- ===================================================================
-- TABELA: audit_logs
-- ===================================================================

CREATE TABLE audit_logs (
    -- Identificação
    id BIGSERIAL PRIMARY KEY,
    
    -- Tipo de evento (enum)
    event_type VARCHAR(50) NOT NULL,
    
    -- Usuário que executou a ação
    user_id BIGINT,
    username VARCHAR(50),
    
    -- Recurso alvo da ação
    target_id VARCHAR(100),
    target_type VARCHAR(50),
    
    -- Descrição da ação
    action VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    
    -- Detalhes adicionais em JSON
    details TEXT,
    
    -- Informações de origem
    ip_address VARCHAR(45),  -- IPv6 max length
    user_agent VARCHAR(255),
    
    -- Status da operação
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message VARCHAR(500),
    
    -- Timestamp
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Chave estrangeira para usuários (opcional, SET NULL se usuário for deletado)
    CONSTRAINT fk_audit_user 
        FOREIGN KEY (user_id) 
        REFERENCES usuarios(id) 
        ON DELETE SET NULL
);

-- ===================================================================
-- ÍNDICES PARA PERFORMANCE
-- ===================================================================

-- Índice para busca por tipo de evento
CREATE INDEX idx_audit_event_type ON audit_logs(event_type);

-- Índice para busca por usuário
CREATE INDEX idx_audit_user_id ON audit_logs(user_id);

-- Índice para busca por timestamp (ordenação comum)
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);

-- Índice composto para busca de logs de um usuário em um período
CREATE INDEX idx_audit_user_timestamp ON audit_logs(user_id, timestamp);

-- Índice para filtro por sucesso/falha
CREATE INDEX idx_audit_success ON audit_logs(success);

-- Índice composto para busca por recurso alvo
CREATE INDEX idx_audit_target ON audit_logs(target_type, target_id);

-- ===================================================================
-- COMENTÁRIOS (Documentação no banco)
-- ===================================================================

COMMENT ON TABLE audit_logs IS 
'Registros de auditoria de todas as ações críticas no sistema';

COMMENT ON COLUMN audit_logs.event_type IS 
'Tipo do evento (AUTH_LOGIN, RBAC_ROLE_CREATED, etc.)';

COMMENT ON COLUMN audit_logs.user_id IS 
'ID do usuário que executou a ação (NULL para eventos de sistema)';

COMMENT ON COLUMN audit_logs.username IS 
'Username desnormalizado para performance em consultas';

COMMENT ON COLUMN audit_logs.target_id IS 
'ID do recurso alvo da ação (ex: ID da role criada)';

COMMENT ON COLUMN audit_logs.target_type IS 
'Tipo do recurso alvo (Role, Permission, Usuario, Pacote)';

COMMENT ON COLUMN audit_logs.action IS 
'Descrição curta da ação realizada';

COMMENT ON COLUMN audit_logs.description IS 
'Descrição detalhada do evento (legível para humanos)';

COMMENT ON COLUMN audit_logs.details IS 
'Metadados adicionais em formato JSON';

COMMENT ON COLUMN audit_logs.ip_address IS 
'Endereço IP de origem da requisição';

COMMENT ON COLUMN audit_logs.user_agent IS 
'User-Agent do cliente (navegador, app, etc.)';

COMMENT ON COLUMN audit_logs.success IS 
'Indica se a operação foi bem-sucedida';

COMMENT ON COLUMN audit_logs.error_message IS 
'Mensagem de erro caso success = false';

COMMENT ON COLUMN audit_logs.timestamp IS 
'Data e hora do evento';

-- ===================================================================
-- VALIDAÇÕES E CONSTRAINTS
-- ===================================================================

-- Garantir que event_type não esteja vazio
ALTER TABLE audit_logs ADD CONSTRAINT chk_event_type_not_empty 
    CHECK (LENGTH(TRIM(event_type)) > 0);

-- Garantir que action não esteja vazia
ALTER TABLE audit_logs ADD CONSTRAINT chk_action_not_empty 
    CHECK (LENGTH(TRIM(action)) > 0);

-- Se success = false, deveria ter error_message (soft constraint, não obrigatório)
-- Não vamos forçar via constraint pois pode haver casos onde não há mensagem específica

-- ===================================================================
-- FIM DA MIGRATION V6
-- ===================================================================

