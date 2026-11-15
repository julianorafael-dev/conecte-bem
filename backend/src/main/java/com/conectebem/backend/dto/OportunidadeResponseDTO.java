package com.conectebem.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Dados de uma oportunidade devolvidos pela API ao frontend.
 *
 * <p>Este DTO não expõe entidades JPA (Ong ou Categoria) diretamente.
 * Os IDs e nomes das ONG e Categoria são incluídos como campos separados
 * para evitar expor a camada de persistência na resposta da API.</p>
 *
 * <p>{@code vagas} é a capacidade total da oportunidade. {@code inscritos}
 * conta apenas as inscrições ativas (PENDENTE e CONFIRMADA) e
 * {@code vagasDisponiveis} é calculado no servidor como capacidade menos
 * inscritos, sem nunca ficar negativo.</p>
 */
public record OportunidadeResponseDTO(
        Integer id,
        Integer ongId,
        String nomeOng,
        Integer categoriaId,
        String nomeCategoria,
        String titulo,
        String descricao,
        LocalDate data,
        LocalTime horario,
        String cidade,
        String estado,
        Integer vagas,
        String status,
        LocalDateTime criadoEm,
        Integer inscritos,
        Integer vagasDisponiveis
) {
}