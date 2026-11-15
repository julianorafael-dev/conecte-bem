package com.conectebem.backend.service;

import java.time.LocalDate;

final class IdadeMinima {
    private IdadeMinima() {}

    static void validar(LocalDate nascimento, LocalDate hoje) {
        if (nascimento == null) {
            throw new IllegalArgumentException("Informe sua data de nascimento.");
        }
        if (nascimento.isAfter(hoje.minusYears(18))) {
            throw new IllegalArgumentException("É preciso ter 18 anos ou mais para criar uma conta.");
        }
    }
}
