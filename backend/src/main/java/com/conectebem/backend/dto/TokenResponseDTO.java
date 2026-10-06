package com.conectebem.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponseDTO {

    private String token;
    private String tipo;

    public TokenResponseDTO(String token) {
        this.token = token;
        this.tipo = "Bearer";
    }
}