package com.neuroefficiency.dto.response;

import com.neuroefficiency.domain.model.UsuarioPacote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de resposta para UsuarioPacote
 * Evita problemas de lazy loading e serialização circular
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPacoteResponse {
    private Long id;
    private Long usuarioId;
    private String usuarioUsername;
    private String pacoteType;
    private Integer limitePacientes;
    private LocalDate dataVencimento;
    private String observacoes;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converte UsuarioPacote entity para UsuarioPacoteResponse DTO
     * Usa Hibernate.isInitialized() para evitar LazyInitializationException
     */
    public static UsuarioPacoteResponse fromEntity(UsuarioPacote pacote) {
        Long usuarioId = null;
        String usuarioUsername = null;
        
        // Verifica se o Usuario está inicializado antes de acessar
        if (pacote.getUsuario() != null && Hibernate.isInitialized(pacote.getUsuario())) {
            usuarioId = pacote.getUsuario().getId();
            usuarioUsername = pacote.getUsuario().getUsername();
        }
        
        return UsuarioPacoteResponse.builder()
                .id(pacote.getId())
                .usuarioId(usuarioId)
                .usuarioUsername(usuarioUsername)
                .pacoteType(pacote.getPacoteType())
                .limitePacientes(pacote.getLimitePacientes())
                .dataVencimento(pacote.getDataVencimento())
                .observacoes(pacote.getObservacoes())
                .ativo(pacote.getAtivo())
                .createdAt(pacote.getCreatedAt())
                .updatedAt(pacote.getUpdatedAt())
                .build();
    }
}

