package com.neuroefficiency.domain.repository;

import com.neuroefficiency.domain.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade PasswordResetToken
 * 
 * Fornece operações de acesso a dados para tokens de reset de senha.
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca token por hash SHA-256
     * 
     * IMPORTANTE: Este método funciona porque usamos SHA-256 (determinístico)
     * ao invés de BCrypt (salt aleatório).
     * 
     * @param tokenHash hash SHA-256 do token
     * @return Optional contendo o token se encontrado
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Busca tokens ativos (não usados e não expirados) de um usuário
     * 
     * Usado para validar se usuário já tem token ativo antes de gerar novo.
     * 
     * @param usuarioId ID do usuário
     * @param now data/hora atual para verificar expiração
     * @return lista de tokens válidos
     */
    @Query("SELECT t FROM PasswordResetToken t " +
           "WHERE t.usuario.id = :usuarioId " +
           "AND t.usedAt IS NULL " +
           "AND t.expiresAt > :now")
    List<PasswordResetToken> findActiveTokensByUsuarioId(
        @Param("usuarioId") Long usuarioId,
        @Param("now") LocalDateTime now
    );

    /**
     * Invalida todos os tokens ativos de um usuário
     * 
     * Usado quando:
     * - Novo token é gerado (apenas 1 ativo por usuário)
     * - Senha é resetada com sucesso
     * 
     * @param usuarioId ID do usuário
     * @param usedAt data/hora para marcar como usado
     * @return número de tokens invalidados
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t " +
           "SET t.usedAt = :usedAt " +
           "WHERE t.usuario.id = :usuarioId " +
           "AND t.usedAt IS NULL")
    int invalidateAllByUsuarioId(
        @Param("usuarioId") Long usuarioId,
        @Param("usedAt") LocalDateTime usedAt
    );

    /**
     * Deleta tokens expirados ou já usados (cleanup job)
     * 
     * Executado diariamente às 3h da manhã via @Scheduled.
     * Remove tokens que:
     * - Expiraram (expiresAt < now)
     * - Já foram usados (usedAt IS NOT NULL)
     * 
     * @param now data/hora atual
     * @return número de tokens deletados
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t " +
           "WHERE t.expiresAt < :now " +
           "OR t.usedAt IS NOT NULL")
    int deleteExpiredOrUsed(@Param("now") LocalDateTime now);

    /**
     * Conta tokens criados por um usuário em um período
     * 
     * Usado para rate limiting: máximo 3 tentativas por hora.
     * 
     * @param usuarioId ID do usuário
     * @param after data/hora de início do período
     * @return número de tokens criados após 'after'
     */
    long countByUsuarioIdAndCreatedAtAfter(Long usuarioId, LocalDateTime after);

    /**
     * Busca tokens de um usuário ordenados por data de criação (mais recente primeiro)
     * 
     * Usado para análise e debug.
     * 
     * @param usuarioId ID do usuário
     * @return lista de tokens ordenada por criação (desc)
     */
    List<PasswordResetToken> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
}

