package com.neuroefficiency.service;

import com.neuroefficiency.domain.model.*;
import com.neuroefficiency.domain.repository.*;
import com.neuroefficiency.exception.ResourceNotFoundException;
import com.neuroefficiency.exception.RoleAlreadyExistsException;
import com.neuroefficiency.exception.PermissionAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service para gerenciamento de RBAC (Role-Based Access Control)
 * 
 * Responsável por gerenciar roles, permissões e pacotes de usuários.
 * Implementa a lógica de negócio para o sistema de autorização.
 * 
 * @author Joao Fuhrmann
 * @version 1.0 - Fase 3 RBAC
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RbacService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioPacoteRepository usuarioPacoteRepository;

    // ===========================================
    // GERENCIAMENTO DE ROLES
    // ===========================================

    /**
     * Cria uma nova role
     */
    public Role createRole(String name, String description) {
        log.info("Criando nova role: {}", name);
        
        if (roleRepository.existsByName(name)) {
            throw new RoleAlreadyExistsException("Role já existe: " + name);
        }

        Role role = Role.builder()
                .name(name.toUpperCase())
                .description(description)
                .active(true)
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("Role criada com sucesso: {} (ID: {})", name, savedRole.getId());
        
        return savedRole;
    }

    /**
     * Busca role por nome
     */
    @Transactional(readOnly = true)
    public Optional<Role> findRoleByName(String name) {
        return roleRepository.findByName(name.toUpperCase());
    }

    /**
     * Lista todas as roles ativas
     */
    @Transactional(readOnly = true)
    public List<Role> findAllActiveRoles() {
        return roleRepository.findByActiveTrue();
    }

    /**
     * Lista todas as roles com suas permissões
     */
    @Transactional(readOnly = true)
    public List<Role> findAllRolesWithPermissions() {
        return roleRepository.findAllWithPermissions();
    }

    /**
     * Adiciona permissão a uma role
     */
    public Role addPermissionToRole(String roleName, String permissionName) {
        log.info("Adicionando permissão {} à role {}", permissionName, roleName);
        
        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada: " + roleName));
        
        Permission permission = permissionRepository.findByName(permissionName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Permissão não encontrada: " + permissionName));

        role.addPermission(permission);
        Role savedRole = roleRepository.save(role);
        
        log.info("Permissão {} adicionada à role {} com sucesso", permissionName, roleName);
        return savedRole;
    }

    /**
     * Remove permissão de uma role
     */
    public Role removePermissionFromRole(String roleName, String permissionName) {
        log.info("Removendo permissão {} da role {}", permissionName, roleName);
        
        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada: " + roleName));
        
        Permission permission = permissionRepository.findByName(permissionName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Permissão não encontrada: " + permissionName));

        role.removePermission(permission);
        Role savedRole = roleRepository.save(role);
        
        log.info("Permissão {} removida da role {} com sucesso", permissionName, roleName);
        return savedRole;
    }

    // ===========================================
    // GERENCIAMENTO DE PERMISSÕES
    // ===========================================

    /**
     * Cria uma nova permissão
     */
    public Permission createPermission(String name, String description, String resource) {
        log.info("Criando nova permissão: {} para recurso {}", name, resource);
        
        if (permissionRepository.existsByName(name)) {
            throw new PermissionAlreadyExistsException("Permissão já existe: " + name);
        }

        Permission permission = Permission.builder()
                .name(name.toUpperCase())
                .description(description)
                .resource(resource.toLowerCase())
                .active(true)
                .build();

        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permissão criada com sucesso: {} (ID: {})", name, savedPermission.getId());
        
        return savedPermission;
    }

    /**
     * Lista todas as permissões ativas
     */
    @Transactional(readOnly = true)
    public List<Permission> findAllActivePermissions() {
        return permissionRepository.findByActiveTrue();
    }

    /**
     * Lista permissões por recurso
     */
    @Transactional(readOnly = true)
    public List<Permission> findPermissionsByResource(String resource) {
        return permissionRepository.findByResourceAndActiveTrue(resource.toLowerCase());
    }

    // ===========================================
    // GERENCIAMENTO DE USUÁRIOS E ROLES
    // ===========================================

    /**
     * Adiciona role a um usuário
     */
    public Usuario addRoleToUsuario(Long usuarioId, String roleName) {
        log.info("Adicionando role {} ao usuário ID: {}", roleName, usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + usuarioId));
        
        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada: " + roleName));

        usuario.addRole(role);
        Usuario savedUsuario = usuarioRepository.save(usuario);
        
        log.info("Role {} adicionada ao usuário {} com sucesso", roleName, usuario.getUsername());
        return savedUsuario;
    }

    /**
     * Remove role de um usuário
     */
    public Usuario removeRoleFromUsuario(Long usuarioId, String roleName) {
        log.info("Removendo role {} do usuário ID: {}", roleName, usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + usuarioId));
        
        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada: " + roleName));

        usuario.removeRole(role);
        Usuario savedUsuario = usuarioRepository.save(usuario);
        
        log.info("Role {} removida do usuário {} com sucesso", roleName, usuario.getUsername());
        return savedUsuario;
    }

    /**
     * Lista usuários com uma role específica
     */
    @Transactional(readOnly = true)
    public List<Usuario> findUsuariosByRole(String roleName) {
        return usuarioRepository.findByRoleName(roleName.toUpperCase());
    }

    /**
     * Lista usuários ADMIN
     */
    @Transactional(readOnly = true)
    public List<Usuario> findAdminUsuarios() {
        return usuarioRepository.findAdminUsuarios();
    }

    /**
     * Lista usuários CLINICO
     */
    @Transactional(readOnly = true)
    public List<Usuario> findClinicoUsuarios() {
        return usuarioRepository.findClinicoUsuarios();
    }

    // ===========================================
    // GERENCIAMENTO DE PACOTES
    // ===========================================

    /**
     * Cria ou atualiza pacote de usuário
     */
    public UsuarioPacote createOrUpdateUsuarioPacote(Long usuarioId, String pacoteType, 
                                                    Integer limitePacientes, LocalDate dataVencimento, 
                                                    String observacoes) {
        log.info("Criando/atualizando pacote {} para usuário ID: {}", pacoteType, usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + usuarioId));

        UsuarioPacote pacote = usuarioPacoteRepository.findByUsuarioId(usuarioId)
                .orElse(UsuarioPacote.builder()
                        .usuario(usuario)
                        .ativo(true)
                        .build());

        pacote.setPacoteType(pacoteType.toUpperCase());
        pacote.setLimitePacientes(limitePacientes);
        pacote.setDataVencimento(dataVencimento);
        pacote.setObservacoes(observacoes);
        pacote.setAtivo(true);

        UsuarioPacote savedPacote = usuarioPacoteRepository.save(pacote);
        
        log.info("Pacote {} criado/atualizado para usuário {} com sucesso", pacoteType, usuario.getUsername());
        return savedPacote;
    }

    /**
     * Lista pacotes vencidos
     */
    @Transactional(readOnly = true)
    public List<UsuarioPacote> findPacotesVencidos() {
        return usuarioPacoteRepository.findPacotesVencidos(LocalDate.now());
    }

    /**
     * Lista pacotes que vencem em X dias
     */
    @Transactional(readOnly = true)
    public List<UsuarioPacote> findPacotesVencendoEm(int dias) {
        LocalDate dataVencimento = LocalDate.now().plusDays(dias);
        return usuarioPacoteRepository.findPacotesVencendoEm(dataVencimento);
    }

    /**
     * Lista pacotes por tipo
     */
    @Transactional(readOnly = true)
    public List<UsuarioPacote> findPacotesByType(String pacoteType) {
        return usuarioPacoteRepository.findByPacoteTypeAndAtivoTrue(pacoteType.toUpperCase());
    }

    // ===========================================
    // MÉTODOS DE UTILIDADE
    // ===========================================

    /**
     * Verifica se usuário tem uma role específica
     */
    @Transactional(readOnly = true)
    public boolean usuarioHasRole(Long usuarioId, String roleName) {
        return usuarioRepository.usuarioHasRole(usuarioId, roleName.toUpperCase());
    }

    /**
     * Verifica se usuário tem uma permissão específica
     */
    @Transactional(readOnly = true)
    public boolean usuarioHasPermission(Long usuarioId, String permissionName) {
        Usuario usuario = usuarioRepository.findByIdWithRoles(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + usuarioId));
        
        return usuario.hasPermission(permissionName.toUpperCase());
    }

    /**
     * Retorna estatísticas do sistema RBAC
     */
    @Transactional(readOnly = true)
    public RbacStats getRbacStats() {
        long totalRoles = roleRepository.count();
        long totalPermissions = permissionRepository.count();
        long totalUsuarios = usuarioRepository.count();
        long pacotesVencidos = usuarioPacoteRepository.countPacotesVencidos(LocalDate.now());
        
        return RbacStats.builder()
                .totalRoles(totalRoles)
                .totalPermissions(totalPermissions)
                .totalUsuarios(totalUsuarios)
                .pacotesVencidos(pacotesVencidos)
                .build();
    }

    /**
     * DTO para estatísticas RBAC
     */
    @lombok.Data
    @lombok.Builder
    public static class RbacStats {
        private long totalRoles;
        private long totalPermissions;
        private long totalUsuarios;
        private long pacotesVencidos;
    }
}
