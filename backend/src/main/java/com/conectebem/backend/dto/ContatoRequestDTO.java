package com.conectebem.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContatoRequestDTO(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email @Size(max = 150)
        @Pattern(regexp = "^[^\\r\\n]+$") String email,
        @NotBlank @Size(max = 2000) String mensagem
) {}
