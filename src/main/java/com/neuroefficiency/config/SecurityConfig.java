package com.neuroefficiency.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Permitir acesso ao H2 Console sem autenticação
                .requestMatchers("/h2-console/**").permitAll()
                // Permitir acesso público à página inicial
                .requestMatchers("/").permitAll()
                // Todas as outras requisições precisam de autenticação
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.realmName("Neuroefficiency"))
            // Configuração específica para H2 Console
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Necessário para H2 Console funcionar
            );
        
        return http.build();
    }
}
