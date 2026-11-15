package com.conectebem.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Dados enviados pelo voluntário para se candidatar a uma oportunidade.
 *
 * <p>O voluntário não é informado pelo cliente: ele é obtido do JWT no
 * servidor. Isso impede que uma conta tente criar inscrição em nome de outra.</p>
 */
public record InscricaoRequestDTO(
        @NotNull(message = "O ID da oportunidade é obrigatório.")
        Integer oportunidadeId
) {
}
