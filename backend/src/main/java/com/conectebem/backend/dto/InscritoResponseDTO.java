package com.conectebem.backend.dto;

import com.conectebem.backend.model.StatusInscricao;

import java.time.LocalDateTime;

/**
 * Dados de um voluntário exibidos à ONG proprietária da oportunidade.
 */
public record InscritoResponseDTO(
        Integer id,
        Integer usuarioId,
        String nomeVoluntario,
        String emailVoluntario,
        StatusInscricao status,
        LocalDateTime dataInscricao
) {
}
