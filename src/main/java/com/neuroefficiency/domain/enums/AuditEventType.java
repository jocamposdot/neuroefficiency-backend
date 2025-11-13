package com.neuroefficiency.domain.enums;

/**
 * Enum para tipos de eventos de auditoria no sistema Neuroefficiency
 * 
 * Define todos os tipos de eventos que devem ser auditados, incluindo:
 * - Autenticação (login, logout, etc.)
 * - RBAC (roles, permissions, pacotes)
 * - Segurança (tentativas de acesso negado)
 * - Sistema (erros, mudanças de configuração)
 * 
 * @author Neuroefficiency Team
 * @version 4.0 - Fase 4: Audit Logging Avançado
 * @since 2025-11-12
 */
public enum AuditEventType {
    
    // ===========================================
    // AUTENTICAÇÃO
    // ===========================================
    
    /**
     * Usuário realizou login com sucesso
     */
    AUTH_LOGIN("Autenticação", "Login realizado com sucesso"),
    
    /**
     * Usuário realizou logout
     */
    AUTH_LOGOUT("Autenticação", "Logout realizado"),
    
    /**
     * Tentativa de login falhou
     */
    AUTH_FAILED_LOGIN("Autenticação", "Tentativa de login falhou"),
    
    /**
     * Novo usuário registrado
     */
    AUTH_REGISTER("Autenticação", "Novo usuário registrado"),
    
    /**
     * Senha alterada
     */
    AUTH_PASSWORD_CHANGE("Autenticação", "Senha alterada"),
    
    /**
     * Solicitação de reset de senha
     */
    AUTH_PASSWORD_RESET_REQUEST("Autenticação", "Solicitação de reset de senha"),
    
    /**
     * Reset de senha confirmado
     */
    AUTH_PASSWORD_RESET_CONFIRM("Autenticação", "Reset de senha confirmado"),
    
    // ===========================================
    // RBAC - ROLES
    // ===========================================
    
    /**
     * Nova role criada
     */
    RBAC_ROLE_CREATED("RBAC - Roles", "Role criada"),
    
    /**
     * Role atualizada
     */
    RBAC_ROLE_UPDATED("RBAC - Roles", "Role atualizada"),
    
    /**
     * Role deletada
     */
    RBAC_ROLE_DELETED("RBAC - Roles", "Role deletada"),
    
    /**
     * Role atribuída a usuário
     */
    RBAC_ROLE_ASSIGNED("RBAC - Roles", "Role atribuída a usuário"),
    
    /**
     * Role removida de usuário
     */
    RBAC_ROLE_REMOVED("RBAC - Roles", "Role removida de usuário"),
    
    // ===========================================
    // RBAC - PERMISSIONS
    // ===========================================
    
    /**
     * Nova permissão criada
     */
    RBAC_PERMISSION_CREATED("RBAC - Permissions", "Permissão criada"),
    
    /**
     * Permissão atualizada
     */
    RBAC_PERMISSION_UPDATED("RBAC - Permissions", "Permissão atualizada"),
    
    /**
     * Permissão deletada
     */
    RBAC_PERMISSION_DELETED("RBAC - Permissions", "Permissão deletada"),
    
    /**
     * Permissão adicionada a role
     */
    RBAC_PERMISSION_ADDED_TO_ROLE("RBAC - Permissions", "Permissão adicionada a role"),
    
    /**
     * Permissão removida de role
     */
    RBAC_PERMISSION_REMOVED_FROM_ROLE("RBAC - Permissions", "Permissão removida de role"),
    
    // ===========================================
    // RBAC - PACOTES
    // ===========================================
    
    /**
     * Novo pacote criado para usuário
     */
    RBAC_PACKAGE_CREATED("RBAC - Pacotes", "Pacote criado"),
    
    /**
     * Pacote atualizado
     */
    RBAC_PACKAGE_UPDATED("RBAC - Pacotes", "Pacote atualizado"),
    
    /**
     * Pacote deletado
     */
    RBAC_PACKAGE_DELETED("RBAC - Pacotes", "Pacote deletado"),
    
    /**
     * Pacote expirou
     */
    RBAC_PACKAGE_EXPIRED("RBAC - Pacotes", "Pacote expirado"),
    
    // ===========================================
    // SEGURANÇA
    // ===========================================
    
    /**
     * Tentativa de acesso a recurso protegido foi negada
     */
    SECURITY_ACCESS_DENIED("Segurança", "Acesso negado"),
    
    /**
     * Tentativa de acesso não autorizado detectada
     */
    SECURITY_UNAUTHORIZED_ATTEMPT("Segurança", "Tentativa não autorizada"),
    
    /**
     * Atividade suspeita detectada
     */
    SECURITY_SUSPICIOUS_ACTIVITY("Segurança", "Atividade suspeita"),
    
    /**
     * Token inválido ou expirado usado
     */
    SECURITY_INVALID_TOKEN("Segurança", "Token inválido"),
    
    /**
     * Rate limit excedido
     */
    SECURITY_RATE_LIMIT_EXCEEDED("Segurança", "Rate limit excedido"),
    
    // ===========================================
    // SISTEMA
    // ===========================================
    
    /**
     * Configuração do sistema alterada
     */
    SYSTEM_CONFIG_CHANGED("Sistema", "Configuração alterada"),
    
    /**
     * Erro do sistema ocorreu
     */
    SYSTEM_ERROR("Sistema", "Erro do sistema");
    
    // ===========================================
    // ATRIBUTOS DO ENUM
    // ===========================================
    
    private final String category;
    private final String description;
    
    /**
     * Construtor do enum
     * 
     * @param category Categoria do evento (ex: "Autenticação", "RBAC - Roles")
     * @param description Descrição legível do evento
     */
    AuditEventType(String category, String description) {
        this.category = category;
        this.description = description;
    }
    
    /**
     * Retorna a categoria do evento
     * 
     * @return Categoria do evento
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Retorna a descrição do evento
     * 
     * @return Descrição do evento
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica se o evento é relacionado a autenticação
     * 
     * @return true se for evento de autenticação
     */
    public boolean isAuthEvent() {
        return this.name().startsWith("AUTH_");
    }
    
    /**
     * Verifica se o evento é relacionado a RBAC
     * 
     * @return true se for evento de RBAC
     */
    public boolean isRbacEvent() {
        return this.name().startsWith("RBAC_");
    }
    
    /**
     * Verifica se o evento é relacionado a segurança
     * 
     * @return true se for evento de segurança
     */
    public boolean isSecurityEvent() {
        return this.name().startsWith("SECURITY_");
    }
    
    /**
     * Verifica se o evento é relacionado a sistema
     * 
     * @return true se for evento de sistema
     */
    public boolean isSystemEvent() {
        return this.name().startsWith("SYSTEM_");
    }
    
    /**
     * Retorna representação string formatada do evento
     * 
     * @return String no formato "CATEGORIA: Descrição"
     */
    @Override
    public String toString() {
        return category + ": " + description;
    }
}
