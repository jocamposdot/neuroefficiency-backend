package com.neuroefficiency.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroefficiency.domain.model.Role;
import com.neuroefficiency.domain.model.Permission;
import com.neuroefficiency.domain.repository.RoleRepository;
import com.neuroefficiency.domain.repository.PermissionRepository;
import com.neuroefficiency.domain.repository.UsuarioRepository;
import com.neuroefficiency.domain.repository.UsuarioPacoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de Integração para RbacController
 * 
 * Testa os endpoints RBAC com autenticação real:
 * - Criação de roles e permissões
 * - Atribuição de roles a usuários
 * - Gerenciamento de pacotes
 * - Consultas e estatísticas
 * - Validação de autorização ADMIN
 * 
 * @author Joao Fuhrmann
 * @version 1.0 - Fase 3 RBAC
 * @since 2025-10-16
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("RbacController - Testes de Integração")
class RbacControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioPacoteRepository usuarioPacoteRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Limpar dados de teste anteriores
        usuarioPacoteRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    // ===========================================
    // TESTES DE AUTORIZAÇÃO
    // ===========================================

    @Test
    @DisplayName("Deve negar acesso sem autenticação")
    void deveNegarAcessoSemAutenticacao() throws Exception {
        // Spring Security retorna 403 (Forbidden) quando não há autenticação e o endpoint requer ADMIN
        mockMvc.perform(get("/api/admin/rbac/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve negar acesso com usuário sem role ADMIN")
    @WithMockUser(username = "user", roles = {"USER"})
    void deveNegarAcessoComUsuarioSemRoleAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/rbac/roles"))
                .andExpect(status().isForbidden());
    }

    // ===========================================
    // TESTES DE ENDPOINTS DE ROLES
    // ===========================================

    @Test
    @DisplayName("Deve listar roles com usuário ADMIN")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveListarRolesComUsuarioAdmin() throws Exception {
        // Given - Criar roles de teste
        Role adminRole = Role.builder()
                .name("ADMIN")
                .description("Administrador")
                .active(true)
                .build();
        roleRepository.save(adminRole);

        Role clinicoRole = Role.builder()
                .name("CLINICO")
                .description("Usuário clínico")
                .active(true)
                .build();
        roleRepository.save(clinicoRole);

        // When & Then
        mockMvc.perform(get("/api/admin/rbac/roles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("ADMIN")))
                .andExpect(jsonPath("$[1].name", is("CLINICO")));
    }

    @Test
    @DisplayName("Deve criar nova role com usuário ADMIN")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveCriarNovaRoleComUsuarioAdmin() throws Exception {
        // Given
        String roleJson = """
                {
                    "name": "NEW_ROLE",
                    "description": "Nova role para testes"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/admin/rbac/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("NEW_ROLE")))
                .andExpect(jsonPath("$.description", is("Nova role para testes")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar criar role duplicada")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveRetornarErroAoTentarCriarRoleDuplicada() throws Exception {
        // Given - Criar role existente
        Role existingRole = Role.builder()
                .name("EXISTING_ROLE")
                .description("Role existente")
                .active(true)
                .build();
        roleRepository.save(existingRole);

        String roleJson = """
                {
                    "name": "EXISTING_ROLE",
                    "description": "Tentativa de duplicar"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/admin/rbac/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson))
                .andExpect(status().isConflict());
    }

    // ===========================================
    // TESTES DE ENDPOINTS DE PERMISSÕES
    // ===========================================

    @Test
    @DisplayName("Deve listar permissões com usuário ADMIN")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveListarPermissoesComUsuarioAdmin() throws Exception {
        // Given - Criar permissões de teste
        Permission readPermission = Permission.builder()
                .name("READ_TEST")
                .description("Permissão de leitura")
                .resource("test")
                .active(true)
                .build();
        permissionRepository.save(readPermission);

        Permission writePermission = Permission.builder()
                .name("WRITE_TEST")
                .description("Permissão de escrita")
                .resource("test")
                .active(true)
                .build();
        permissionRepository.save(writePermission);

        // When & Then
        mockMvc.perform(get("/api/admin/rbac/permissions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("READ_TEST")))
                .andExpect(jsonPath("$[1].name", is("WRITE_TEST")));
    }

    @Test
    @DisplayName("Deve criar nova permissão com usuário ADMIN")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveCriarNovaPermissaoComUsuarioAdmin() throws Exception {
        // Given
        String permissionJson = """
                {
                    "name": "NEW_PERMISSION",
                    "description": "Nova permissão para testes",
                    "resource": "test"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/admin/rbac/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(permissionJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("NEW_PERMISSION")))
                .andExpect(jsonPath("$.description", is("Nova permissão para testes")))
                .andExpect(jsonPath("$.resource", is("test")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    // ===========================================
    // TESTES DE ENDPOINTS DE ESTATÍSTICAS
    // ===========================================

    @Test
    @DisplayName("Deve retornar estatísticas RBAC com usuário ADMIN")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveRetornarEstatisticasRbacComUsuarioAdmin() throws Exception {
        // Given - Criar dados de teste
        Role adminRole = Role.builder()
                .name("ADMIN")
                .description("Administrador")
                .active(true)
                .build();
        roleRepository.save(adminRole);

        Permission testPermission = Permission.builder()
                .name("TEST_PERMISSION")
                .description("Permissão de teste")
                .resource("test")
                .active(true)
                .build();
        permissionRepository.save(testPermission);

        // When & Then
        mockMvc.perform(get("/api/admin/rbac/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalRoles", is(1)))
                .andExpect(jsonPath("$.totalPermissions", is(1)))
                .andExpect(jsonPath("$.totalUsuarios", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.pacotesVencidos", is(greaterThanOrEqualTo(0))));
    }

    // ===========================================
    // TESTES DE VALIDAÇÃO DE DADOS
    // ===========================================

    @Test
    @DisplayName("Deve retornar erro 400 para dados inválidos na criação de role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveRetornarErro400ParaDadosInvalidosNaCriacaoDeRole() throws Exception {
        // Given - Dados inválidos (nome vazio, que viola @NotBlank)
        String invalidRoleJson = """
                {
                    "name": "",
                    "description": "Nome vazio"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/admin/rbac/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRoleJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para dados inválidos na criação de permissão")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveRetornarErro400ParaDadosInvalidosNaCriacaoDePermissao() throws Exception {
        // Given - Dados inválidos (nome vazio)
        String invalidPermissionJson = """
                {
                    "name": "",
                    "description": "Nome vazio",
                    "resource": "test"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/admin/rbac/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPermissionJson))
                .andExpect(status().isBadRequest());
    }

    // ===========================================
    // TESTES DE ENDPOINTS DE PACOTES
    // ===========================================

    @Test
    @DisplayName("Deve criar pacote de usuário com usuário ADMIN")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveCriarPacoteDeUsuarioComUsuarioAdmin() throws Exception {
        // Given - Criar usuário de teste
        com.neuroefficiency.domain.model.Usuario testUser = com.neuroefficiency.domain.model.Usuario.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .enabled(true)
                .build();
        com.neuroefficiency.domain.model.Usuario savedUser = usuarioRepository.save(testUser);

        String packageJson = """
                {
                    "pacoteType": "PREMIUM",
                    "limitePacientes": 500,
                    "dataVencimento": "2025-12-31",
                    "permissoesCustomizadas": "Permissões premium"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/admin/rbac/users/{userId}/package", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(packageJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pacoteType", is("PREMIUM")))
                .andExpect(jsonPath("$.limitePacientes", is(500)))
                .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    @DisplayName("Deve retornar erro 404 para usuário inexistente ao criar pacote")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveRetornarErro404ParaUsuarioInexistenteAoCriarPacote() throws Exception {
        // Given
        String packageJson = """
                {
                    "pacoteType": "PREMIUM",
                    "limitePacientes": 500,
                    "dataVencimento": "2025-12-31",
                    "permissoesCustomizadas": "Permissões premium"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/admin/rbac/users/999/package")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(packageJson))
                .andExpect(status().isNotFound());
    }

    // ===========================================
    // TESTES DE ENDPOINTS DE ATRIBUIÇÃO DE ROLES
    // ===========================================

    @Test
    @DisplayName("Deve adicionar role a usuário com usuário ADMIN")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveAdicionarRoleAUsuarioComUsuarioAdmin() throws Exception {
        // Given - Criar usuário e role de teste
        com.neuroefficiency.domain.model.Usuario testUser = com.neuroefficiency.domain.model.Usuario.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .enabled(true)
                .build();
        com.neuroefficiency.domain.model.Usuario savedUser = usuarioRepository.save(testUser);

        Role testRole = Role.builder()
                .name("CLINICO")
                .description("Usuário clínico")
                .active(true)
                .build();
        roleRepository.save(testRole);

        // When & Then - URL correta com roleName como path variable
        mockMvc.perform(post("/api/admin/rbac/users/{userId}/roles/{roleName}", 
                        savedUser.getId(), "CLINICO"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    @DisplayName("Deve retornar erro 404 para usuário inexistente ao adicionar role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveRetornarErro404ParaUsuarioInexistenteAoAdicionarRole() throws Exception {
        // Given - Criar role válida
        Role testRole = Role.builder()
                .name("CLINICO")
                .description("Usuário clínico")
                .active(true)
                .build();
        roleRepository.save(testRole);

        // When & Then - URL correta com roleName como path variable
        mockMvc.perform(post("/api/admin/rbac/users/{userId}/roles/{roleName}", 
                        999L, "CLINICO"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar erro 404 para role inexistente ao adicionar role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deveRetornarErro404ParaRoleInexistenteAoAdicionarRole() throws Exception {
        // Given - Criar usuário de teste
        com.neuroefficiency.domain.model.Usuario testUser = com.neuroefficiency.domain.model.Usuario.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .enabled(true)
                .build();
        com.neuroefficiency.domain.model.Usuario savedUser = usuarioRepository.save(testUser);

        // When & Then - URL correta com roleName como path variable
        mockMvc.perform(post("/api/admin/rbac/users/{userId}/roles/{roleName}", 
                        savedUser.getId(), "NONEXISTENT_ROLE"))
                .andExpect(status().isNotFound());
    }
}
