package com.conectebem.backend.dto;

/**
 * Dados consolidados exibidos no painel da ONG autenticada.
 *
 * <p>Os indicadores são calculados no backend a partir das oportunidades e
 * inscrições vinculadas à ONG, evitando que o frontend envie um identificador
 * de ONG que poderia ser alterado manualmente.</p>
 */
public record PainelOngResponseDTO(
        String nomeOng,
        long totalOportunidades,
        long oportunidadesAtivas,
        long oportunidadesEncerradas,
        long totalVoluntariosInscritos
) {
}
