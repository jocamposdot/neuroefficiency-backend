package com.neuroefficiency.service;

import com.neuroefficiency.domain.model.Usuario;
import com.neuroefficiency.domain.repository.UsuarioRepository;
import com.neuroefficiency.dto.request.LoginRequest;
import com.neuroefficiency.dto.request.RegisterRequest;
import com.neuroefficiency.dto.response.AuthResponse;
import com.neuroefficiency.exception.PasswordMismatchException;
import com.neuroefficiency.exception.UsernameAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes Unitários para AuthenticationService
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService - Testes Unitários")
class AuthenticationServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        validRegisterRequest = RegisterRequest.builder()
                .username("testuser")
                .password("Test@1234")
                .confirmPassword("Test@1234")
                .build();

        validLoginRequest = LoginRequest.builder()
                .username("testuser")
                .password("Test@1234")
                .build();

        mockUsuario = Usuario.builder()
                .id(1L)
                .username("testuser")
                .passwordHash("$2a$12$hashedPassword")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    // ==================== TESTES DE REGISTRO ====================

    @Test
    @DisplayName("Deve registrar usuário com sucesso")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);

        // Act
        AuthResponse response = authenticationService.register(validRegisterRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Usuário registrado com sucesso");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");

        verify(usuarioRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("Test@1234");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando senhas não coincidem")
    void shouldThrowExceptionWhenPasswordsDoNotMatch() {
        // Arrange
        validRegisterRequest.setConfirmPassword("DifferentPassword@123");

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.register(validRegisterRequest))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessageContaining("A senha e a confirmação de senha não coincidem");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando username já existe")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Arrange
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.register(validRegisterRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("já está em uso");

        verify(usuarioRepository).existsByUsername("testuser");
        verify(usuarioRepository, never()).save(any());
    }

    // ==================== TESTES DE LOGIN ====================

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void shouldLoginSuccessfully() {
        // Arrange
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getPrincipal()).thenReturn(mockUsuario);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);

        // Act
        AuthResponse response = authenticationService.login(validLoginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Login realizado com sucesso");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando credenciais são inválidas")
    void shouldThrowExceptionWhenCredentialsAreInvalid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(validLoginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não existe")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(validLoginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}

