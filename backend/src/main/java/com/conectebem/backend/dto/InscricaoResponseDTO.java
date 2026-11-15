package com.conectebem.backend.dto;

import com.conectebem.backend.model.StatusInscricao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Dados de uma candidatura exibidos para o voluntário em Minhas Candidaturas.
 */
public record InscricaoResponseDTO(
        Integer id,
        Integer oportunidadeId,
        String tituloOportunidade,
        String nomeOng,
        LocalDate data,
        LocalTime horario,
        String cidade,
        String estado,
        String descricaoOportunidade,
        StatusInscricao statusInscricao,
        String statusOportunidade,
        LocalDateTime dataInscricao
) {
}
