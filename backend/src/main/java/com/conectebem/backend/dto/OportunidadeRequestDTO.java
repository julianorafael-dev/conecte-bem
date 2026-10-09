package com.conectebem.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

/**
 * Dados recebidos pela API ao criar ou atualizar uma oportunidade.
 *
 * <p>As anotações de validação são aplicadas pelo {@code @Valid} do controller
 * antes de a regra de negócio ser executada. Não inclui {@code id}, {@code nomeOng},
 * {@code nomeCategoria} nem {@code criadoEm}, pois esses campos são tratados
 * internamente pelo sistema.</p>
 */
public record OportunidadeRequestDTO(
        @NotNull Integer ongId,
        @NotNull Integer categoriaId,
        @NotBlank @Size(max = 200) String titulo,
        String descricao,
        @NotNull LocalDate data,
        @NotNull LocalTime horario,
        @Size(max = 100) String cidade,
        @Pattern(regexp = "^[A-Z]{2}$", message = "O estado deve conter duas letras maiúsculas.") String estado,
        @NotNull @Min(1) Integer vagas,
        @NotBlank String status
) {
}