package com.neuroefficiency.security;

import com.neuroefficiency.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação customizada do UserDetailsService do Spring Security
 * 
 * Responsável por carregar os dados do usuário durante o processo de autenticação.
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carrega um usuário pelo username para autenticação
     * 
     * @param username o username do usuário
     * @return UserDetails contendo os dados do usuário
     * @throws UsernameNotFoundException se o usuário não for encontrado
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Tentando carregar usuário: {}", sanitizeUsername(username));
        
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: {}", sanitizeUsername(username));
                    return new UsernameNotFoundException(
                        "Usuário não encontrado: " + username
                    );
                });
    }

    /**
     * Sanitiza o username para logs (remove caracteres especiais)
     * Previne log injection
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

