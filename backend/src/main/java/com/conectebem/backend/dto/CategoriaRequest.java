package com.conectebem.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest (
    @NotBlank
    @Size(max = 100)
    String nome,

    @NotBlank
    @Size(max = 250)
    String descricao
){}
