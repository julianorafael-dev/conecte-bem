package com.conectebem.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OngPerfilRequestDTO(
        @NotBlank @Size(max = 150) String nome,
        String descricao,
        @Size(max = 20) String telefone,
        @Size(max = 100) String cidade,
        @Pattern(regexp = "^[A-Z]{2}$") String estado
) {
}
