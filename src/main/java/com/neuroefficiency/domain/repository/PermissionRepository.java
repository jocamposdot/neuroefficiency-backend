package com.neuroefficiency.domain.repository;

import com.neuroefficiency.domain.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para entidade Permission
 * 
 * @author Joao Fuhrmann
 * @version 1.0 - Fase 3 RBAC
 * @since 2025-10-16
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Busca permissão por nome
     */
    Optional<Permission> findByName(String name);

    /**
     * Verifica se existe permissão com o nome especificado
     */
    boolean existsByName(String name);

    /**
     * Busca permissões ativas
     */
    List<Permission> findByActiveTrue();

    /**
     * Busca permissões por recurso
     */
    List<Permission> findByResource(String resource);

    /**
     * Busca permissões ativas por recurso
     */
    List<Permission> findByResourceAndActiveTrue(String resource);

    /**
     * Busca permissão por nome (case insensitive)
     */
    @Query("SELECT p FROM Permission p WHERE LOWER(p.name) = LOWER(:name)")
    Optional<Permission> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Busca permissões que contêm o nome especificado
     */
    @Query("SELECT p FROM Permission p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Permission> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Busca permissões por recurso (case insensitive)
     */
    @Query("SELECT p FROM Permission p WHERE LOWER(p.resource) = LOWER(:resource)")
    List<Permission> findByResourceIgnoreCase(@Param("resource") String resource);

    /**
     * Busca permissões de leitura
     */
    @Query("SELECT p FROM Permission p WHERE p.name LIKE 'READ_%' AND p.active = true")
    List<Permission> findReadPermissions();

    /**
     * Busca permissões de escrita
     */
    @Query("SELECT p FROM Permission p WHERE (p.name LIKE 'WRITE_%' OR p.name LIKE 'CREATE_%' OR p.name LIKE 'UPDATE_%' OR p.name LIKE 'DELETE_%') AND p.active = true")
    List<Permission> findWritePermissions();

    /**
     * Busca permissões por recurso e tipo (leitura/escrita)
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.name LIKE :type AND p.active = true")
    List<Permission> findByResourceAndType(@Param("resource") String resource, @Param("type") String type);

    /**
     * Busca todas as permissões com suas roles
     */
    @Query("SELECT DISTINCT p FROM Permission p LEFT JOIN FETCH p.roles")
    List<Permission> findAllWithRoles();

    /**
     * Busca permissões ativas com suas roles
     */
    @Query("SELECT DISTINCT p FROM Permission p LEFT JOIN FETCH p.roles WHERE p.active = true")
    List<Permission> findActiveWithRoles();

    /**
     * Conta quantas roles têm uma permissão específica
     */
    @Query("SELECT COUNT(r) FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    long countRolesByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * Busca permissões que não têm roles associadas
     */
    @Query("SELECT p FROM Permission p WHERE p.id NOT IN (SELECT DISTINCT p2.id FROM Permission p2 JOIN p2.roles)")
    List<Permission> findPermissionsWithoutRoles();

    /**
     * Busca recursos únicos
     */
    @Query("SELECT DISTINCT p.resource FROM Permission p WHERE p.active = true ORDER BY p.resource")
    List<String> findDistinctResources();
}
