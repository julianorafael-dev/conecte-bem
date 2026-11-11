package com.conectebem.backend.dto;

import com.conectebem.backend.model.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
public class RegistroRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    private String senha;

    @NotNull(message = "O tipo de usuário é obrigatório")
    private TipoUsuario tipo;

    // CPF opcional. Nascimento é exigido pelo serviço para novos voluntários (QA-010).
    @CPF(message = "CPF inválido")
    @Size(max = 14, message = "O CPF deve ter no máximo 14 caracteres")
    private String cpf;

    @PastOrPresent(message = "A data de nascimento não pode estar no futuro")
    private LocalDate dataNascimento;

    @PastOrPresent(message = "A data de criação da ONG não pode estar no futuro")
    private LocalDate dataCriacao;

    // Campos da ONG; telefone é utilizado por ambos os tipos de conta.
    @Size(max = 18, message = "O CNPJ deve ter no máximo 18 caracteres")
    private String cnpj;

    private String descricao;

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
    private String cidade;

    private String estado;
}
