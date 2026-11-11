package com.conectebem.backend.dto;

import com.conectebem.backend.model.Usuario;
import lombok.Getter;
import java.time.LocalDate;

/** Dados pessoais disponíveis somente no perfil autenticado /voluntarios/me. */
@Getter
public class VoluntarioPerfilResponseDTO extends UsuarioResponseDTO {
    private final String cpf;
    private final LocalDate dataNascimento;
    private final String telefone;

    private VoluntarioPerfilResponseDTO(Usuario usuario) {
        super(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTipo());
        this.cpf = usuario.getCpf();
        this.dataNascimento = usuario.getDataNascimento();
        this.telefone = usuario.getTelefone();
    }

    public static VoluntarioPerfilResponseDTO fromEntity(Usuario usuario) {
        return new VoluntarioPerfilResponseDTO(usuario);
    }
}
