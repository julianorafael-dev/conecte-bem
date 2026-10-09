package com.conectebem.backend.dto;

import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Integer id;
    private String nome;
    private String email;
    private TipoUsuario tipo;

    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipo()
        );
    }
}