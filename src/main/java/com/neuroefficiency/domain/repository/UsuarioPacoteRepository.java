package com.neuroefficiency.domain.repository;

import com.neuroefficiency.domain.model.UsuarioPacote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para entidade UsuarioPacote
 * 
 * @author Joao Fuhrmann
 * @version 1.0 - Fase 3 RBAC
 * @since 2025-10-16
 */
@Repository
public interface UsuarioPacoteRepository extends JpaRepository<UsuarioPacote, Long> {

    /**
     * Busca pacote por usuário
     */
    Optional<UsuarioPacote> findByUsuarioId(Long usuarioId);

    /**
     * Verifica se existe pacote para o usuário
     */
    boolean existsByUsuarioId(Long usuarioId);

    /**
     * Busca pacotes ativos
     */
    List<UsuarioPacote> findByAtivoTrue();

    /**
     * Busca pacotes por tipo
     */
    List<UsuarioPacote> findByPacoteType(String pacoteType);

    /**
     * Busca pacotes ativos por tipo
     */
    List<UsuarioPacote> findByPacoteTypeAndAtivoTrue(String pacoteType);

    /**
     * Busca pacotes que vencem em uma data específica
     */
    List<UsuarioPacote> findByDataVencimento(LocalDate dataVencimento);

    /**
     * Busca pacotes que vencem até uma data específica
     */
    List<UsuarioPacote> findByDataVencimentoLessThanEqual(LocalDate dataVencimento);

    /**
     * Busca pacotes que vencem em um período
     */
    @Query("SELECT up FROM UsuarioPacote up WHERE up.dataVencimento BETWEEN :dataInicio AND :dataFim")
    List<UsuarioPacote> findByDataVencimentoBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    /**
     * Busca pacotes vencidos
     */
    @Query("SELECT up FROM UsuarioPacote up WHERE up.dataVencimento < :hoje AND up.ativo = true")
    List<UsuarioPacote> findPacotesVencidos(@Param("hoje") LocalDate hoje);

    /**
     * Busca pacotes que vencem em X dias
     */
    @Query("SELECT up FROM UsuarioPacote up WHERE up.dataVencimento = :dataVencimento AND up.ativo = true")
    List<UsuarioPacote> findPacotesVencendoEm(@Param("dataVencimento") LocalDate dataVencimento);

    /**
     * Busca pacotes com limite de pacientes
     */
    @Query("SELECT up FROM UsuarioPacote up WHERE up.limitePacientes IS NOT NULL AND up.limitePacientes > 0")
    List<UsuarioPacote> findPacotesComLimite();

    /**
     * Busca pacotes ilimitados
     */
    @Query("SELECT up FROM UsuarioPacote up WHERE up.limitePacientes IS NULL OR up.limitePacientes <= 0")
    List<UsuarioPacote> findPacotesIlimitados();

    /**
     * Busca pacotes customizados
     */
    @Query("SELECT up FROM UsuarioPacote up WHERE up.pacoteType = 'CUSTOM'")
    List<UsuarioPacote> findPacotesCustomizados();

    /**
     * Conta pacotes por tipo
     */
    @Query("SELECT up.pacoteType, COUNT(up) FROM UsuarioPacote up WHERE up.ativo = true GROUP BY up.pacoteType")
    List<Object[]> countPacotesByType();

    /**
     * Conta pacotes vencidos
     */
    @Query("SELECT COUNT(up) FROM UsuarioPacote up WHERE up.dataVencimento < :hoje AND up.ativo = true")
    long countPacotesVencidos(@Param("hoje") LocalDate hoje);

    /**
     * Conta pacotes que vencem em X dias
     */
    @Query("SELECT COUNT(up) FROM UsuarioPacote up WHERE up.dataVencimento = :dataVencimento AND up.ativo = true")
    long countPacotesVencendoEm(@Param("dataVencimento") LocalDate dataVencimento);

    /**
     * Busca usuários com pacotes próximos do vencimento
     */
    @Query("SELECT up FROM UsuarioPacote up WHERE up.dataVencimento BETWEEN :hoje AND :dataLimite AND up.ativo = true ORDER BY up.dataVencimento ASC")
    List<UsuarioPacote> findPacotesProximosVencimento(@Param("hoje") LocalDate hoje, @Param("dataLimite") LocalDate dataLimite);

    /**
     * Busca pacotes válidos (ativos e não vencidos)
     */
    @Query("SELECT up FROM UsuarioPacote up WHERE up.ativo = true AND (up.dataVencimento IS NULL OR up.dataVencimento >= :hoje)")
    List<UsuarioPacote> findPacotesValidos(@Param("hoje") LocalDate hoje);

    /**
     * Busca pacotes inválidos (inativos ou vencidos)
     */
    @Query("SELECT up FROM UsuarioPacote up WHERE up.ativo = false OR up.dataVencimento < :hoje")
    List<UsuarioPacote> findPacotesInvalidos(@Param("hoje") LocalDate hoje);
}
