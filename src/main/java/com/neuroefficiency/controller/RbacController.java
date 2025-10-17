package com.neuroefficiency.controller;

import com.neuroefficiency.domain.model.Permission;
import com.neuroefficiency.domain.model.Role;
import com.neuroefficiency.domain.model.Usuario;
import com.neuroefficiency.domain.model.UsuarioPacote;
import com.neuroefficiency.dto.response.UserResponse;
import com.neuroefficiency.dto.response.RoleResponse;
import com.neuroefficiency.dto.response.PermissionResponse;
import com.neuroefficiency.service.RbacService;
import com.neuroefficiency.service.RbacService.RbacStats;

import java.util.stream.Collectors;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller para gerenciamento de RBAC (Role-Based Access Control)
 * 
 * Endpoints restritos apenas para usuários com role ADMIN.
 * Permite gerenciar roles, permissões e pacotes de usuários.
 * 
 * @author Joao Fuhrmann
 * @version 1.0 - Fase 3 RBAC
 * @since 2025-10-16
 */
@RestController
@RequestMapping("/api/admin/rbac")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class RbacController {

    private final RbacService rbacService;

    // ===========================================
    // GERENCIAMENTO DE ROLES
    // ===========================================

    /**
     * Lista todas as roles ativas
     */
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        log.info("Listando todas as roles ativas");
        List<Role> roles = rbacService.findAllActiveRoles();
        List<RoleResponse> response = roles.stream()
                .map(RoleResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todas as roles com suas permissões
     */
    @GetMapping("/roles/with-permissions")
    public ResponseEntity<List<RoleResponse>> getAllRolesWithPermissions() {
        log.info("Listando todas as roles com permissões");
        List<Role> roles = rbacService.findAllRolesWithPermissions();
        List<RoleResponse> response = roles.stream()
                .map(RoleResponse::fromEntityWithPermissions)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Cria uma nova role
     */
    @PostMapping("/roles")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("Criando nova role: {}", request.getName());
        Role role = rbacService.createRole(request.getName(), request.getDescription());
        return ResponseEntity.ok(RoleResponse.fromEntity(role));
    }

    /**
     * Adiciona permissão a uma role
     */
    @PostMapping("/roles/{roleName}/permissions/{permissionName}")
    public ResponseEntity<RoleResponse> addPermissionToRole(
            @PathVariable String roleName,
            @PathVariable String permissionName) {
        
        log.info("Adicionando permissão {} à role {}", permissionName, roleName);
        Role role = rbacService.addPermissionToRole(roleName, permissionName);
        return ResponseEntity.ok(RoleResponse.fromEntityWithPermissions(role));
    }

    /**
     * Remove permissão de uma role
     */
    @DeleteMapping("/roles/{roleName}/permissions/{permissionName}")
    public ResponseEntity<RoleResponse> removePermissionFromRole(
            @PathVariable String roleName,
            @PathVariable String permissionName) {
        
        log.info("Removendo permissão {} da role {}", permissionName, roleName);
        Role role = rbacService.removePermissionFromRole(roleName, permissionName);
        return ResponseEntity.ok(RoleResponse.fromEntityWithPermissions(role));
    }

    // ===========================================
    // GERENCIAMENTO DE PERMISSÕES
    // ===========================================

    /**
     * Lista todas as permissões ativas
     */
    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        log.info("Listando todas as permissões ativas");
        List<Permission> permissions = rbacService.findAllActivePermissions();
        List<PermissionResponse> response = permissions.stream()
                .map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Lista permissões por recurso
     */
    @GetMapping("/permissions/resource/{resource}")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByResource(@PathVariable String resource) {
        log.info("Listando permissões para recurso: {}", resource);
        List<Permission> permissions = rbacService.findPermissionsByResource(resource);
        List<PermissionResponse> response = permissions.stream()
                .map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Cria uma nova permissão
     */
    @PostMapping("/permissions")
    public ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        log.info("Criando nova permissão: {} para recurso {}", request.getName(), request.getResource());
        Permission permission = rbacService.createPermission(
                request.getName(), 
                request.getDescription(), 
                request.getResource()
        );
        return ResponseEntity.ok(PermissionResponse.fromEntity(permission));
    }

    // ===========================================
    // GERENCIAMENTO DE USUÁRIOS E ROLES
    // ===========================================

    /**
     * Lista usuários com uma role específica
     */
    @GetMapping("/users/role/{roleName}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String roleName) {
        log.info("Listando usuários com role: {}", roleName);
        List<Usuario> users = rbacService.findUsuariosByRole(roleName);
        List<UserResponse> response = users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Lista usuários ADMIN
     */
    @GetMapping("/users/admin")
    public ResponseEntity<List<UserResponse>> getAdminUsers() {
        log.info("Listando usuários ADMIN");
        List<Usuario> users = rbacService.findAdminUsuarios();
        List<UserResponse> response = users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Lista usuários CLINICO
     */
    @GetMapping("/users/clinico")
    public ResponseEntity<List<UserResponse>> getClinicoUsers() {
        log.info("Listando usuários CLINICO");
        List<Usuario> users = rbacService.findClinicoUsuarios();
        List<UserResponse> response = users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Adiciona role a um usuário
     */
    @PostMapping("/users/{userId}/roles/{roleName}")
    public ResponseEntity<UserResponse> addRoleToUser(
            @PathVariable Long userId,
            @PathVariable String roleName) {
        
        log.info("Adicionando role {} ao usuário ID: {}", roleName, userId);
        Usuario user = rbacService.addRoleToUsuario(userId, roleName);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * Remove role de um usuário
     */
    @DeleteMapping("/users/{userId}/roles/{roleName}")
    public ResponseEntity<UserResponse> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable String roleName) {
        
        log.info("Removendo role {} do usuário ID: {}", roleName, userId);
        Usuario user = rbacService.removeRoleFromUsuario(userId, roleName);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    // ===========================================
    // GERENCIAMENTO DE PACOTES
    // ===========================================

    /**
     * Lista pacotes por tipo
     */
    @GetMapping("/packages/type/{pacoteType}")
    public ResponseEntity<List<UsuarioPacote>> getPackagesByType(@PathVariable String pacoteType) {
        log.info("Listando pacotes do tipo: {}", pacoteType);
        List<UsuarioPacote> packages = rbacService.findPacotesByType(pacoteType);
        return ResponseEntity.ok(packages);
    }

    /**
     * Lista pacotes vencidos
     */
    @GetMapping("/packages/expired")
    public ResponseEntity<List<UsuarioPacote>> getExpiredPackages() {
        log.info("Listando pacotes vencidos");
        List<UsuarioPacote> packages = rbacService.findPacotesVencidos();
        return ResponseEntity.ok(packages);
    }

    /**
     * Lista pacotes que vencem em X dias
     */
    @GetMapping("/packages/expiring/{days}")
    public ResponseEntity<List<UsuarioPacote>> getPackagesExpiringIn(@PathVariable int days) {
        log.info("Listando pacotes que vencem em {} dias", days);
        List<UsuarioPacote> packages = rbacService.findPacotesVencendoEm(days);
        return ResponseEntity.ok(packages);
    }

    /**
     * Cria ou atualiza pacote de usuário
     */
    @PostMapping("/users/{userId}/package")
    public ResponseEntity<UsuarioPacote> createOrUpdateUserPackage(
            @PathVariable Long userId,
            @Valid @RequestBody CreatePackageRequest request) {
        
        log.info("Criando/atualizando pacote {} para usuário ID: {}", request.getPacoteType(), userId);
        UsuarioPacote pacote = rbacService.createOrUpdateUsuarioPacote(
                userId,
                request.getPacoteType(),
                request.getLimitePacientes(),
                request.getDataVencimento(),
                request.getObservacoes()
        );
        return ResponseEntity.ok(pacote);
    }

    // ===========================================
    // ESTATÍSTICAS E UTILIDADES
    // ===========================================

    /**
     * Retorna estatísticas do sistema RBAC
     */
    @GetMapping("/stats")
    public ResponseEntity<RbacStats> getRbacStats() {
        log.info("Retornando estatísticas RBAC");
        RbacStats stats = rbacService.getRbacStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Verifica se usuário tem uma role específica
     */
    @GetMapping("/users/{userId}/has-role/{roleName}")
    public ResponseEntity<Map<String, Object>> userHasRole(
            @PathVariable Long userId,
            @PathVariable String roleName) {
        
        log.info("Verificando se usuário {} tem role {}", userId, roleName);
        boolean hasRole = rbacService.usuarioHasRole(userId, roleName);
        
        Map<String, Object> response = Map.of(
                "userId", userId,
                "roleName", roleName,
                "hasRole", hasRole
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verifica se usuário tem uma permissão específica
     */
    @GetMapping("/users/{userId}/has-permission/{permissionName}")
    public ResponseEntity<Map<String, Object>> userHasPermission(
            @PathVariable Long userId,
            @PathVariable String permissionName) {
        
        log.info("Verificando se usuário {} tem permissão {}", userId, permissionName);
        boolean hasPermission = rbacService.usuarioHasPermission(userId, permissionName);
        
        Map<String, Object> response = Map.of(
                "userId", userId,
                "permissionName", permissionName,
                "hasPermission", hasPermission
        );
        
        return ResponseEntity.ok(response);
    }

    // ===========================================
    // DTOs
    // ===========================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRoleRequest {
        @NotBlank(message = "Nome da role é obrigatório")
        private String name;
        
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePermissionRequest {
        @NotBlank(message = "Nome da permissão é obrigatório")
        private String name;
        
        private String description;
        
        @NotBlank(message = "Recurso é obrigatório")
        private String resource;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePackageRequest {
        @NotBlank(message = "Tipo do pacote é obrigatório")
        private String pacoteType;
        
        private Integer limitePacientes;
        
        private LocalDate dataVencimento;
        
        private String observacoes;
    }
}
