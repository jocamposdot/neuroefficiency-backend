package com.neuroefficiency.controller;

import com.neuroefficiency.dto.request.LoginRequest;
import com.neuroefficiency.dto.request.RegisterRequest;
import com.neuroefficiency.dto.request.SetupAdminRequest;
import com.neuroefficiency.dto.response.AuthResponse;
import com.neuroefficiency.dto.response.UserResponse;
import com.neuroefficiency.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller de Autenticação
 * 
 * Expõe endpoints REST para registro, login e logout de usuários.
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Endpoint de registro de novo usuário
     * 
     * POST /api/auth/register
     * 
     * @param request dados do novo usuário
     * @return resposta com dados do usuário criado
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Requisição de registro recebida de IP: {}", getClientIP(httpRequest));
        
        AuthResponse response = authenticationService.register(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint de setup do primeiro administrador
     * 
     * POST /api/auth/setup-admin
     * 
     * Este endpoint só pode ser usado quando não existe nenhum administrador no sistema.
     * É usado para configuração inicial da aplicação.
     * 
     * @param request dados do administrador
     * @param httpRequest requisição HTTP
     * @return resposta com dados do admin criado
     */
    @PostMapping("/setup-admin")
    public ResponseEntity<AuthResponse> setupAdmin(
            @Valid @RequestBody SetupAdminRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Requisição de setup de admin recebida de IP: {}", getClientIP(httpRequest));
        
        AuthResponse response = authenticationService.setupAdmin(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint de login
     * 
     * POST /api/auth/login
     * 
     * @param request credenciais de login
     * @param httpRequest requisição HTTP
     * @param httpResponse resposta HTTP
     * @return resposta com dados do usuário autenticado
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        log.info("Requisição de login recebida de IP: {}", getClientIP(httpRequest));
        
        // Chamar serviço com request e response para salvar contexto na sessão
        AuthResponse response = authenticationService.login(request, httpRequest, httpResponse);
        
        // Adicionar informações da sessão
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            response.setSessionId(session.getId());
            log.debug("Sessão criada: {}", session.getId());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obter informações do usuário autenticado
     * 
     * GET /api/auth/me
     * 
     * @return dados do usuário atualmente autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.debug("Requisição de informações do usuário atual");
        
        UserResponse response = authenticationService.getCurrentUser();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de logout
     * 
     * POST /api/auth/logout
     * 
     * @return mensagem de confirmação
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        log.info("Requisição de logout recebida de IP: {}", getClientIP(request));
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            session.invalidate();
            log.info("Sessão invalidada: {}", sessionId);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout realizado com sucesso");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de health check para testes
     * 
     * GET /api/auth/health
     * 
     * @return status do serviço de autenticação
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Authentication Service");
        response.put("version", "1.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtém o IP do cliente da requisição
     * 
     * @param request a requisição HTTP
     * @return o IP do cliente
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

