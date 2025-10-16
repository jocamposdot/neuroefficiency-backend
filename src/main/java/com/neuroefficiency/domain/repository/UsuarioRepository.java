package com.neuroefficiency.domain.repository;

import com.neuroefficiency.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Usuario
 * 
 * Fornece operações de acesso a dados para usuários do sistema.
 * 
 * @author Joao Fuhrmann
 * @version 3.0 - Adicionados métodos RBAC (Fase 3)
 * @since 2025-10-11
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário por username
     * 
     * @param username o username do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Verifica se existe um usuário com o username informado
     * 
     * @param username o username a ser verificado
     * @return true se o username já existe, false caso contrário
     */
    boolean existsByUsername(String username);

    /**
     * Busca um usuário por username ignorando diferença entre maiúsculas e minúsculas
     * 
     * @param username o username do usuário
     * @return Optional contendo o usuário se encontrado
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.username) = LOWER(?1)")
    Optional<Usuario> findByUsernameIgnoreCase(String username);

    /**
     * Conta quantos usuários estão ativos
     * 
     * @return número de usuários com enabled = true
     */
    long countByEnabledTrue();

    // ========================================
    // TAREFA 2: Métodos de busca por email
    // ========================================

    /**
     * Busca um usuário por email
     * 
     * @param email o email do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica se existe um usuário com o email informado
     * 
     * @param email o email a ser verificado
     * @return true se o email já existe, false caso contrário
     */
    boolean existsByEmail(String email);

    /**
     * Busca um usuário por email ignorando diferença entre maiúsculas e minúsculas
     * 
     * @param email o email do usuário
     * @return Optional contendo o usuário se encontrado
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.email) = LOWER(?1)")
    Optional<Usuario> findByEmailIgnoreCase(String email);

    // ========================================
    // FASE 3: Métodos RBAC
    // ========================================

    /**
     * Busca usuário com suas roles (fetch join)
     */
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<Usuario> findByIdWithRoles(@Param("id") Long id);

    /**
     * Busca usuário por username com suas roles
     */
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Busca usuário por email com suas roles
     */
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<Usuario> findByEmailWithRoles(@Param("email") String email);

    /**
     * Busca usuários com uma role específica
     */
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.name = :roleName")
    List<Usuario> findByRoleName(@Param("roleName") String roleName);

    /**
     * Busca usuários com roles ativas
     */
    @Query("SELECT DISTINCT u FROM Usuario u JOIN u.roles r WHERE r.active = true")
    List<Usuario> findUsuariosWithActiveRoles();

    /**
     * Busca usuários ADMIN
     */
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.name = 'ADMIN'")
    List<Usuario> findAdminUsuarios();

    /**
     * Busca usuários CLINICO
     */
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.name = 'CLINICO'")
    List<Usuario> findClinicoUsuarios();

    /**
     * Verifica se usuário tem uma role específica
     */
    @Query("SELECT COUNT(u) > 0 FROM Usuario u JOIN u.roles r WHERE u.id = :usuarioId AND r.name = :roleName")
    boolean usuarioHasRole(@Param("usuarioId") Long usuarioId, @Param("roleName") String roleName);

    /**
     * Conta usuários por role
     */
    @Query("SELECT r.name, COUNT(u) FROM Usuario u JOIN u.roles r GROUP BY r.name")
    List<Object[]> countUsuariosByRole();

    /**
     * Busca usuários sem roles
     */
    @Query("SELECT u FROM Usuario u WHERE u.id NOT IN (SELECT DISTINCT u2.id FROM Usuario u2 JOIN u2.roles)")
    List<Usuario> findUsuariosWithoutRoles();

    /**
     * Busca usuários com pacote válido
     */
    @Query("SELECT u FROM Usuario u JOIN u.pacote p WHERE p.ativo = true AND (p.dataVencimento IS NULL OR p.dataVencimento >= CURRENT_DATE)")
    List<Usuario> findUsuariosWithPacoteValido();

    /**
     * Busca usuários com pacote vencido
     */
    @Query("SELECT u FROM Usuario u JOIN u.pacote p WHERE p.ativo = true AND p.dataVencimento < CURRENT_DATE")
    List<Usuario> findUsuariosWithPacoteVencido();
}

