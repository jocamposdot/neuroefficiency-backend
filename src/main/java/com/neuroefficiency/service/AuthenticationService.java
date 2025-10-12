package com.neuroefficiency.service;

import com.neuroefficiency.domain.model.Usuario;
import com.neuroefficiency.domain.repository.UsuarioRepository;
import com.neuroefficiency.dto.request.LoginRequest;
import com.neuroefficiency.dto.request.RegisterRequest;
import com.neuroefficiency.dto.response.AuthResponse;
import com.neuroefficiency.dto.response.UserResponse;
import com.neuroefficiency.exception.PasswordMismatchException;
import com.neuroefficiency.exception.UsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Serviço de Autenticação
 * 
 * Responsável por gerenciar o registro e login de usuários.
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    /**
     * Registra um novo usuário no sistema
     * 
     * @param request dados do novo usuário
     * @return resposta contendo informações do usuário criado
     * @throws UsernameAlreadyExistsException se o username já existe
     * @throws PasswordMismatchException se as senhas não coincidem
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Iniciando registro de novo usuário: {}", sanitizeUsername(request.getUsername()));

        // Validar se as senhas coincidem
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Tentativa de registro com senhas não coincidentes");
            throw new PasswordMismatchException(
                "A senha e a confirmação de senha não coincidem"
            );
        }

        // Verificar se o username já existe
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            log.warn("Tentativa de registro com username já existente: {}", 
                    sanitizeUsername(request.getUsername()));
            throw new UsernameAlreadyExistsException(
                "Username '" + request.getUsername() + "' já está em uso"
            );
        }

        // Criar novo usuário
        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        // Salvar no banco
        Usuario savedUsuario = usuarioRepository.save(usuario);

        log.info("Usuário registrado com sucesso: {} (ID: {})", 
                sanitizeUsername(savedUsuario.getUsername()), 
                savedUsuario.getId());

        // Retornar resposta
        return AuthResponse.success(
            "Usuário registrado com sucesso",
            UserResponse.from(savedUsuario)
        );
    }

    /**
     * Autentica um usuário no sistema
     * 
     * @param request credenciais de login
     * @param httpRequest requisição HTTP para salvar o contexto na sessão
     * @param httpResponse resposta HTTP para salvar o contexto na sessão
     * @return resposta contendo informações do usuário autenticado
     * @throws org.springframework.security.authentication.BadCredentialsException 
     *         se as credenciais forem inválidas
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.info("Tentativa de login para usuário: {}", sanitizeUsername(request.getUsername()));

        try {
            // Autenticar usando o AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            // Criar SecurityContext e definir autenticação
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // CRÍTICO: Salvar o contexto na sessão HTTP
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            // Buscar usuário autenticado
            Usuario usuario = (Usuario) authentication.getPrincipal();

            log.info("Login bem-sucedido para usuário: {} (ID: {})", 
                    sanitizeUsername(usuario.getUsername()), 
                    usuario.getId());

            // Retornar resposta
            return AuthResponse.success(
                "Login realizado com sucesso",
                UserResponse.from(usuario)
            );

        } catch (Exception e) {
            log.warn("Falha no login para usuário: {}", sanitizeUsername(request.getUsername()));
            throw e;
        }
    }

    /**
     * Retorna informações do usuário atualmente autenticado
     * 
     * @return resposta contendo dados do usuário
     * @throws org.springframework.security.authentication.InsufficientAuthenticationException
     *         se não houver usuário autenticado
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Nenhum usuário autenticado");
        }

        Usuario usuario = (Usuario) authentication.getPrincipal();
        log.debug("Recuperando dados do usuário atual: {}", sanitizeUsername(usuario.getUsername()));

        return UserResponse.from(usuario);
    }

    /**
     * Sanitiza o username para logs (previne log injection)
     * 
     * @param username o username a ser sanitizado
     * @return username sanitizado
     */
    private String sanitizeUsername(String username) {
        if (username == null) {
            return "null";
        }
        return username.replaceAll("[^a-zA-Z0-9_-]", "");
    }
}

