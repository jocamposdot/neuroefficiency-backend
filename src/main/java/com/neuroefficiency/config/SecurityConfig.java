package com.neuroefficiency.config;

import com.neuroefficiency.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Configuração de Segurança do Spring Security
 * 
 * Define as regras de autenticação e autorização do sistema Neuroefficiency.
 * 
 * @author Joao Fuhrmann
 * @version 3.0 - Adicionada autorização RBAC (Fase 3)
 * @since 2025-10-11
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Configuração do PasswordEncoder com BCrypt
     * Força 12 (recomendado para ambientes de saúde)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configuração do SecurityContextRepository
     * Responsável por persistir o SecurityContext na sessão HTTP
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Bean do AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    /**
     * Configuração da cadeia de filtros de segurança
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos - Autenticação
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/health").permitAll()
                
                // TAREFA 2: Endpoints públicos - Recuperação de Senha
                .requestMatchers("/api/auth/password-reset/request").permitAll()
                .requestMatchers("/api/auth/password-reset/confirm").permitAll()
                .requestMatchers("/api/auth/password-reset/validate-token/**").permitAll()
                .requestMatchers("/api/auth/password-reset/health").permitAll()
                
                // FASE 3: Endpoints RBAC - Apenas ADMIN
                .requestMatchers("/api/admin/rbac/**").hasRole("ADMIN")
                
                // Outros endpoints públicos
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/").permitAll()
                
                // Todas as outras requisições precisam autenticação
                .anyRequest().authenticated()
            )
            // Desabilitar CSRF temporariamente (será configurado depois)
            .csrf(AbstractHttpConfigurer::disable)
            // Configurações do H2 Console
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );
        
        return http.build();
    }
}

