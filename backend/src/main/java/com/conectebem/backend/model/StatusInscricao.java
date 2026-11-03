package com.conectebem.backend.model;

/**
 * Estados possíveis de uma candidatura de voluntário.
 *
 * <p>Os valores são persistidos como texto na tabela {@code inscricoes}.
 * Inscrições pendentes e confirmadas ocupam vaga; inscrições canceladas não.</p>
 */
public enum StatusInscricao {
    PENDENTE,
    CONFIRMADA,
    CANCELADA
}
