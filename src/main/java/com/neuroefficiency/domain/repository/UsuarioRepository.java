package com.neuroefficiency.domain.repository;

import com.neuroefficiency.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para a entidade Usuario
 * 
 * Fornece operações de acesso a dados para usuários do sistema.
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário por username
     * 
     * @param username o username do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Verifica se existe um usuário com o username informado
     * 
     * @param username o username a ser verificado
     * @return true se o username já existe, false caso contrário
     */
    boolean existsByUsername(String username);

    /**
     * Busca um usuário por username ignorando diferença entre maiúsculas e minúsculas
     * 
     * @param username o username do usuário
     * @return Optional contendo o usuário se encontrado
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.username) = LOWER(?1)")
    Optional<Usuario> findByUsernameIgnoreCase(String username);

    /**
     * Conta quantos usuários estão ativos
     * 
     * @return número de usuários com enabled = true
     */
    long countByEnabledTrue();
}

