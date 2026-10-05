package com.conectebem.backend.dto;

/**
 * Dados de uma ONG devolvidos pela API ao frontend.
 *
 * <p>O {@code usuarioId} não é exposto porque é um vínculo interno do sistema.
 * A autenticação deve obter esse usuário pelo token, e não pelo retorno público da ONG.</p>
 */
public record OngResponseDTO(
        Integer id,
        String nome,
        String cnpj,
        String descricao,
        String telefone,
        String cidade,
        String estado
) {
}
