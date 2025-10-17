package com.neuroefficiency.service;

import com.neuroefficiency.domain.model.Permission;
import com.neuroefficiency.domain.model.Role;
import com.neuroefficiency.domain.model.Usuario;
import com.neuroefficiency.domain.model.UsuarioPacote;
import com.neuroefficiency.domain.repository.PermissionRepository;
import com.neuroefficiency.domain.repository.RoleRepository;
import com.neuroefficiency.domain.repository.UsuarioRepository;
import com.neuroefficiency.domain.repository.UsuarioPacoteRepository;
import com.neuroefficiency.exception.PermissionAlreadyExistsException;
import com.neuroefficiency.exception.ResourceNotFoundException;
import com.neuroefficiency.exception.RoleAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes Unitários para RbacService
 * 
 * Testa toda a lógica de negócio do sistema RBAC:
 * - Criação e gerenciamento de roles
 * - Criação e gerenciamento de permissões
 * - Atribuição de roles a usuários
 * - Gerenciamento de pacotes de usuário
 * - Validações de negócio e exceções
 * 
 * @author Joao Fuhrmann
 * @version 1.0 - Fase 3 RBAC
 * @since 2025-10-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RbacService - Testes Unitários")
class RbacServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioPacoteRepository usuarioPacoteRepository;

    @InjectMocks
    private RbacService rbacService;

    private Role testRole;
    private Permission testPermission;
    private Usuario testUsuario;
    private UsuarioPacote testPacote;

    @BeforeEach
    void setUp() {
        // Setup Role de teste
        testRole = Role.builder()
                .id(1L)
                .name("TEST_ROLE")
                .description("Role para testes")
                .active(true)
                .build();

        // Setup Permission de teste
        testPermission = Permission.builder()
                .id(1L)
                .name("TEST_PERMISSION")
                .description("Permissão para testes")
                .resource("test")
                .active(true)
                .build();

        // Setup Usuario de teste
        testUsuario = Usuario.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .enabled(true)
                .build();

        // Setup UsuarioPacote de teste
        testPacote = UsuarioPacote.builder()
                .id(1L)
                .usuario(testUsuario)
                .pacoteType("BASIC")
                .limitePacientes(50)
                .dataVencimento(LocalDate.now().plusDays(30))
                .ativo(true)
                .build();
    }

    // ===========================================
    // TESTES DE CRIAÇÃO DE ROLES
    // ===========================================

    @Test
    @DisplayName("Deve criar nova role com sucesso")
    void deveCriarNovaRoleComSucesso() {
        // Given
        String roleName = "NEW_ROLE";
        String description = "Nova role para testes";
        
        when(roleRepository.existsByName(roleName)).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        Role result = rbacService.createRole(roleName, description);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TEST_ROLE");
        
        verify(roleRepository).existsByName(roleName);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar role duplicada")
    void deveLancarExcecaoAoTentarCriarRoleDuplicada() {
        // Given
        String roleName = "EXISTING_ROLE";
        String description = "Role já existente";
        
        when(roleRepository.existsByName(roleName)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> rbacService.createRole(roleName, description))
                .isInstanceOf(RoleAlreadyExistsException.class)
                .hasMessageContaining("Role já existe: EXISTING_ROLE");
        
        verify(roleRepository).existsByName(roleName);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Deve converter nome da role para maiúsculo")
    void deveConverterNomeDaRoleParaMaiusculo() {
        // Given
        String roleName = "lowercase_role";
        String description = "Role em minúsculo";
        
        // O serviço verifica se existe com o nome original
        when(roleRepository.existsByName(roleName)).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        rbacService.createRole(roleName, description);

        // Then
        verify(roleRepository).existsByName(roleName);
        verify(roleRepository).save(any(Role.class));
    }

    // ===========================================
    // TESTES DE CRIAÇÃO DE PERMISSÕES
    // ===========================================

    @Test
    @DisplayName("Deve criar nova permissão com sucesso")
    void deveCriarNovaPermissaoComSucesso() {
        // Given
        String permissionName = "NEW_PERMISSION";
        String description = "Nova permissão para testes";
        String resource = "test";
        
        when(permissionRepository.existsByName(permissionName)).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);

        // When
        Permission result = rbacService.createPermission(permissionName, description, resource);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TEST_PERMISSION");
        
        verify(permissionRepository).existsByName(permissionName);
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar permissão duplicada")
    void deveLancarExcecaoAoTentarCriarPermissaoDuplicada() {
        // Given
        String permissionName = "EXISTING_PERMISSION";
        String description = "Permissão já existente";
        String resource = "test";
        
        when(permissionRepository.existsByName(permissionName)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> rbacService.createPermission(permissionName, description, resource))
                .isInstanceOf(PermissionAlreadyExistsException.class)
                .hasMessageContaining("Permissão já existe: EXISTING_PERMISSION");
        
        verify(permissionRepository).existsByName(permissionName);
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    // ===========================================
    // TESTES DE ATRIBUIÇÃO DE ROLES
    // ===========================================

    @Test
    @DisplayName("Deve adicionar role a usuário com sucesso")
    void deveAdicionarRoleAUsuarioComSucesso() {
        // Given
        Long usuarioId = 1L;
        String roleName = "ADMIN";
        
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(testUsuario));
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(testRole));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(testUsuario);

        // When
        Usuario result = rbacService.addRoleToUsuario(usuarioId, roleName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        
        verify(usuarioRepository).findById(usuarioId);
        verify(roleRepository).findByName(roleName);
        verify(usuarioRepository).save(testUsuario);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar adicionar role a usuário inexistente")
    void deveLancarExcecaoAoTentarAdicionarRoleAUsuarioInexistente() {
        // Given
        Long usuarioId = 999L;
        String roleName = "ADMIN";
        
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> rbacService.addRoleToUsuario(usuarioId, roleName))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado: 999");
        
        verify(usuarioRepository).findById(usuarioId);
        verify(roleRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar adicionar role inexistente")
    void deveLancarExcecaoAoTentarAdicionarRoleInexistente() {
        // Given
        Long usuarioId = 1L;
        String roleName = "NONEXISTENT_ROLE";
        
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(testUsuario));
        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> rbacService.addRoleToUsuario(usuarioId, roleName))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role não encontrada: NONEXISTENT_ROLE");
        
        verify(usuarioRepository).findById(usuarioId);
        verify(roleRepository).findByName(roleName);
    }

    // ===========================================
    // TESTES DE GERENCIAMENTO DE PACOTES
    // ===========================================

    @Test
    @DisplayName("Deve criar novo pacote de usuário com sucesso")
    void deveCriarNovoPacoteDeUsuarioComSucesso() {
        // Given
        Long usuarioId = 1L;
        String pacoteType = "PREMIUM";
        Integer limitePacientes = 500;
        LocalDate dataVencimento = LocalDate.now().plusDays(365);
        String observacoes = "Pacote premium anual";
        
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(testUsuario));
        when(usuarioPacoteRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());
        when(usuarioPacoteRepository.save(any(UsuarioPacote.class))).thenReturn(testPacote);

        // When
        UsuarioPacote result = rbacService.createOrUpdateUsuarioPacote(
                usuarioId, pacoteType, limitePacientes, dataVencimento, observacoes);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsuario()).isEqualTo(testUsuario);
        
        verify(usuarioRepository).findById(usuarioId);
        verify(usuarioPacoteRepository).findByUsuarioId(usuarioId);
        verify(usuarioPacoteRepository).save(any(UsuarioPacote.class));
    }

    @Test
    @DisplayName("Deve atualizar pacote existente com sucesso")
    void deveAtualizarPacoteExistenteComSucesso() {
        // Given
        Long usuarioId = 1L;
        String pacoteType = "ENTERPRISE";
        Integer limitePacientes = null; // Ilimitado
        LocalDate dataVencimento = LocalDate.now().plusDays(730);
        String observacoes = "Upgrade para enterprise";
        
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(testUsuario));
        when(usuarioPacoteRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(testPacote));
        when(usuarioPacoteRepository.save(any(UsuarioPacote.class))).thenReturn(testPacote);

        // When
        UsuarioPacote result = rbacService.createOrUpdateUsuarioPacote(
                usuarioId, pacoteType, limitePacientes, dataVencimento, observacoes);

        // Then
        assertThat(result).isNotNull();
        
        verify(usuarioRepository).findById(usuarioId);
        verify(usuarioPacoteRepository).findByUsuarioId(usuarioId);
        verify(usuarioPacoteRepository).save(testPacote);
    }

    // ===========================================
    // TESTES DE CONSULTAS E LISTAGENS
    // ===========================================

    @Test
    @DisplayName("Deve listar todas as roles ativas")
    void deveListarTodasAsRolesAtivas() {
        // Given
        List<Role> rolesAtivas = Arrays.asList(testRole);
        when(roleRepository.findByActiveTrue()).thenReturn(rolesAtivas);

        // When
        List<Role> result = rbacService.findAllActiveRoles();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("TEST_ROLE");
        
        verify(roleRepository).findByActiveTrue();
    }

    @Test
    @DisplayName("Deve listar todas as permissões ativas")
    void deveListarTodasAsPermissoesAtivas() {
        // Given
        List<Permission> permissoesAtivas = Arrays.asList(testPermission);
        when(permissionRepository.findByActiveTrue()).thenReturn(permissoesAtivas);

        // When
        List<Permission> result = rbacService.findAllActivePermissions();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("TEST_PERMISSION");
        
        verify(permissionRepository).findByActiveTrue();
    }

    @Test
    @DisplayName("Deve retornar estatísticas RBAC corretas")
    void deveRetornarEstatisticasRbacCorretas() {
        // Given
        when(roleRepository.count()).thenReturn(5L);
        when(permissionRepository.count()).thenReturn(12L);
        when(usuarioRepository.count()).thenReturn(100L);
        when(usuarioPacoteRepository.countPacotesVencidos(any(LocalDate.class))).thenReturn(3L);

        // When
        RbacService.RbacStats stats = rbacService.getRbacStats();

        // Then
        assertThat(stats.getTotalRoles()).isEqualTo(5L);
        assertThat(stats.getTotalPermissions()).isEqualTo(12L);
        assertThat(stats.getTotalUsuarios()).isEqualTo(100L);
        assertThat(stats.getPacotesVencidos()).isEqualTo(3L);
        
        verify(roleRepository).count();
        verify(permissionRepository).count();
        verify(usuarioRepository).count();
        verify(usuarioPacoteRepository).countPacotesVencidos(any(LocalDate.class));
    }

    // ===========================================
    // TESTES DE VALIDAÇÃO DE PERMISSÕES
    // ===========================================

    @Test
    @DisplayName("Deve verificar se usuário tem role específica")
    void deveVerificarSeUsuarioTemRoleEspecifica() {
        // Given
        Long usuarioId = 1L;
        String roleName = "ADMIN";
        
        when(usuarioRepository.usuarioHasRole(usuarioId, roleName)).thenReturn(true);

        // When
        boolean result = rbacService.usuarioHasRole(usuarioId, roleName);

        // Then
        assertThat(result).isTrue();
        
        verify(usuarioRepository).usuarioHasRole(usuarioId, roleName);
    }

    @Test
    @DisplayName("Deve verificar se usuário tem permissão específica")
    void deveVerificarSeUsuarioTemPermissaoEspecifica() {
        // Given
        Long usuarioId = 1L;
        String permissionName = "READ_PATIENTS";
        
        // Criar objetos separados para evitar referência circular
        Usuario usuarioSimples = Usuario.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .enabled(true)
                .build();
        
        when(usuarioRepository.findByIdWithRoles(usuarioId)).thenReturn(Optional.of(usuarioSimples));

        // When
        boolean result = rbacService.usuarioHasPermission(usuarioId, permissionName);

        // Then
        assertThat(result).isFalse(); // Usuário sem roles não tem permissões
        
        verify(usuarioRepository).findByIdWithRoles(usuarioId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao verificar permissão de usuário inexistente")
    void deveLancarExcecaoAoVerificarPermissaoDeUsuarioInexistente() {
        // Given
        Long usuarioId = 999L;
        String permissionName = "READ_PATIENTS";
        
        when(usuarioRepository.findByIdWithRoles(usuarioId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> rbacService.usuarioHasPermission(usuarioId, permissionName))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado: 999");
        
        verify(usuarioRepository).findByIdWithRoles(usuarioId);
    }
}
