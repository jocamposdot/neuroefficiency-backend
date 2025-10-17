package com.neuroefficiency.dto.response;

import com.neuroefficiency.domain.model.UsuarioPacote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     */
    public static UsuarioPacoteResponse fromEntity(UsuarioPacote pacote) {
        return UsuarioPacoteResponse.builder()
                .id(pacote.getId())
                .usuarioId(pacote.getUsuario() != null ? pacote.getUsuario().getId() : null)
                .usuarioUsername(pacote.getUsuario() != null ? pacote.getUsuario().getUsername() : null)
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

