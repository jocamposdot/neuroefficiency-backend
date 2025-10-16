package com.neuroefficiency.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroefficiency.domain.repository.UsuarioRepository;
import com.neuroefficiency.dto.request.LoginRequest;
import com.neuroefficiency.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de Integração para AuthController
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController - Testes de Integração")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    // ==================== TESTES DE HEALTH CHECK ====================

    @Test
    @DisplayName("Deve retornar status UP no health check")
    void shouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("Authentication Service"))
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // ==================== TESTES DE REGISTRO ====================

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void shouldRegisterNewUserSuccessfully() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("NewUser@123")
                .confirmPassword("NewUser@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Usuário registrado com sucesso"))
                .andExpect(jsonPath("$.user.username").value("newuser"))
                .andExpect(jsonPath("$.user.id").exists())
                .andExpect(jsonPath("$.user.enabled").value(true));
    }

    @Test
    @DisplayName("Deve retornar 400 quando senhas não coincidem")
    void shouldReturn400WhenPasswordsDoNotMatch() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("NewUser@123")
                .confirmPassword("DifferentPassword@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Senhas não coincidem"))
                .andExpect(jsonPath("$.message").value("A senha e a confirmação de senha não coincidem"));
    }

    @Test
    @DisplayName("Deve retornar 409 quando username já existe")
    void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
        // Primeiro registro
        RegisterRequest firstRequest = RegisterRequest.builder()
                .username("existinguser")
                .email("existinguser@example.com")
                .password("Existing@123")
                .confirmPassword("Existing@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Tentativa de registro duplicado
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username("existinguser")
                .email("different@example.com")
                .password("Different@123")
                .confirmPassword("Different@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Username já existe"))
                .andExpect(jsonPath("$.message").value(containsString("já está em uso")));
    }

    @Test
    @DisplayName("Deve retornar 400 quando dados são inválidos")
    void shouldReturn400WhenDataIsInvalid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("ab")  // Username muito curto
                .email("ab@example.com")
                .password("weak")  // Senha fraca
                .confirmPassword("weak")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    // ==================== TESTES DE LOGIN ====================

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void shouldLoginSuccessfully() throws Exception {
        // Primeiro, registrar o usuário
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("loginuser")
                .email("loginuser@example.com")
                .password("Login@123")
                .confirmPassword("Login@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Agora, fazer login
        LoginRequest loginRequest = LoginRequest.builder()
                .username("loginuser")
                .password("Login@123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login realizado com sucesso"))
                .andExpect(jsonPath("$.user.username").value("loginuser"))
                .andExpect(jsonPath("$.user.id").exists());
    }

    @Test
    @DisplayName("Deve retornar 401 quando credenciais são inválidas")
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        // Primeiro, registrar o usuário
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("loginuser")
                .password("Login@123")
                .confirmPassword("Login@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Tentar login com senha incorreta
        LoginRequest loginRequest = LoginRequest.builder()
                .username("loginuser")
                .password("WrongPassword@123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciais inválidas"))
                .andExpect(jsonPath("$.message").value(containsString("Username ou password incorretos")));
    }

    @Test
    @DisplayName("Deve retornar 401 quando usuário não existe")
    void shouldReturn401WhenUserDoesNotExist() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("nonexistentuser")
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciais inválidas"))
                .andExpect(jsonPath("$.message").value(containsString("Username ou password incorretos")));
    }

    @Test
    @DisplayName("Deve retornar 400 quando dados de login são inválidos")
    void shouldReturn400WhenLoginDataIsInvalid() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("")  // Username vazio
                .password("")  // Senha vazia
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}

