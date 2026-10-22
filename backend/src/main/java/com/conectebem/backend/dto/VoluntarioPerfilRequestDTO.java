package com.conectebem.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VoluntarioPerfilRequestDTO(
        @NotBlank @Size(max = 150) String nome
) {
}
