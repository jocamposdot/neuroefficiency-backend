package com.neuroefficiency.domain.repository;

import com.neuroefficiency.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para entidade Role
 * 
 * @author Joao Fuhrmann
 * @version 1.0 - Fase 3 RBAC
 * @since 2025-10-16
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca role por nome
     */
    Optional<Role> findByName(String name);

    /**
     * Verifica se existe role com o nome especificado
     */
    boolean existsByName(String name);

    /**
     * Busca roles ativas
     */
    List<Role> findByActiveTrue();

    /**
     * Busca roles por nome (case insensitive)
     */
    @Query("SELECT r FROM Role r WHERE LOWER(r.name) = LOWER(:name)")
    Optional<Role> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Busca roles que contêm o nome especificado
     */
    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Role> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Busca roles com suas permissões (fetch join)
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    /**
     * Busca todas as roles com suas permissões
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions")
    List<Role> findAllWithPermissions();

    /**
     * Busca roles ativas com suas permissões
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.active = true")
    List<Role> findActiveWithPermissions();

    /**
     * Conta quantos usuários têm uma role específica
     */
    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.roles r WHERE r.id = :roleId")
    long countUsuariosByRoleId(@Param("roleId") Long roleId);

    /**
     * Busca roles que não têm usuários associados
     */
    @Query("SELECT r FROM Role r WHERE r.id NOT IN (SELECT DISTINCT r2.id FROM Role r2 JOIN r2.usuarios)")
    List<Role> findRolesWithoutUsuarios();

    /**
     * Verifica se existe pelo menos um usuário com a role ADMIN
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM Usuario u JOIN u.roles r WHERE r.name = 'ADMIN'")
    boolean existsUsuarioWithAdminRole();
}
