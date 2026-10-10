package com.conectebem.backend.exception;

public class CategoriaEmUsoException extends RuntimeException {
    public CategoriaEmUsoException(Long id) {
        super("Categoria " + id + " possui oportunidades vinculadas e não pode ser excluída.");
    }
}