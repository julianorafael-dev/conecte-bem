package com.conectebem.backend.exception;

/** Falha de autenticação por e-mail ou senha incorretos (respondida como HTTP 401). */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha incorretos.");
    }
}
