package com.conectebem.backend.exception;

/**
 * Representa conflitos previstos nas regras de inscrição, como duplicidade
 * ou falta de vagas disponíveis.
 */
public class InscricaoConflictException extends RuntimeException {

    public InscricaoConflictException(String mensagem) {
        super(mensagem);
    }
}
