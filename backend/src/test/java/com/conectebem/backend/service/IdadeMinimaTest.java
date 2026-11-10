package com.conectebem.backend.service;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdadeMinimaTest {
    @Test
    void aceitaAniversarioDe18AnosERejeitaUmDiaAntes() {
        LocalDate hoje = LocalDate.of(2026, 11, 10);
        assertDoesNotThrow(() -> IdadeMinima.validar(LocalDate.of(2008, 11, 10), hoje));
        assertThrows(IllegalArgumentException.class,
                () -> IdadeMinima.validar(LocalDate.of(2008, 11, 11), hoje));
        assertThrows(IllegalArgumentException.class, () -> IdadeMinima.validar(null, hoje));
    }

    @Test
    void trataAnosBissextosSemAnteciparAniversario() {
        assertThrows(IllegalArgumentException.class, () -> IdadeMinima.validar(
                LocalDate.of(2008, 2, 29), LocalDate.of(2026, 2, 28)));
        assertDoesNotThrow(() -> IdadeMinima.validar(
                LocalDate.of(2008, 2, 29), LocalDate.of(2026, 3, 1)));
        assertDoesNotThrow(() -> IdadeMinima.validar(
                LocalDate.of(2006, 2, 28), LocalDate.of(2024, 2, 29)));
    }
}
