package com.conectebem.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados recebidos pela API ao criar ou atualizar uma ONG.
 *
 * <p>As anotações de validação são aplicadas pelo {@code @Valid} do controller
 * antes de a regra de negócio ser executada.</p>
 */
public record OngRequestDTO(
        @NotNull Integer usuarioId,
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 18) String cnpj,
        String descricao,
        @Size(max = 20) String telefone,
        @Size(max = 100) String cidade,
        @Pattern(regexp = "^[A-Z]{2}$", message = "O estado deve conter duas letras maiúsculas.") String estado
) {
}
