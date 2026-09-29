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
public class OngRequestDTO {

    @NotNull
    private Integer usuarioId;

    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotBlank
    @Size(max = 18)
    private String cnpj;

    private String descricao;

    @Size(max = 20)
    private String telefone;

    @Size(max = 100)
    private String cidade;

    @Pattern(regexp = "^[A-Z]{2}$", message = "O estado deve conter duas letras maiúsculas.")
    private String estado;

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
