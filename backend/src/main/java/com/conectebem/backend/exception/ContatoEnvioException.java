package com.conectebem.backend.exception;

public class ContatoEnvioException extends RuntimeException {

    public ContatoEnvioException(Throwable causa) {
        super("Não foi possível enviar sua mensagem. Tente novamente mais tarde.", causa);
    }
}
