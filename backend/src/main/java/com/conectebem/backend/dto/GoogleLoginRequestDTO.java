package com.conectebem.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDTO(
        @NotBlank(message = "O token do Google é obrigatório")
        String idToken
    ) {}
