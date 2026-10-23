package com.conectebem.backend.service;

import com.conectebem.backend.exception.ResourceNotFoundException;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.repository.OngRepository;
import com.conectebem.backend.repository.OportunidadeRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OngServiceTest {

    private final OngRepository ongRepository = mock(OngRepository.class);
    private final OportunidadeRepository oportunidadeRepository = mock(OportunidadeRepository.class);
    private final OngService service = new OngService(ongRepository, oportunidadeRepository);

    @Test
    void resumeIndicadoresDaOngAutenticada() {
        Ong ong = new Ong();
        ong.setUsuarioId(7);
        ong.setNome("ONG Esperança");

        when(ongRepository.findByUsuarioId(7)).thenReturn(Optional.of(ong));
        when(oportunidadeRepository.countByOngUsuarioId(7)).thenReturn(8L);
        when(oportunidadeRepository.countByOngUsuarioIdAndStatus(7, "ABERTA")).thenReturn(5L);
        when(oportunidadeRepository.countByOngUsuarioIdAndStatus(7, "ENCERRADA")).thenReturn(2L);
        when(oportunidadeRepository.countInscricoesByOngUsuarioId(7)).thenReturn(24L);

        var resposta = service.buscarResumoPainel(7);

        assertEquals("ONG Esperança", resposta.nomeOng());
        assertEquals(8L, resposta.totalOportunidades());
        assertEquals(5L, resposta.oportunidadesAtivas());
        assertEquals(2L, resposta.oportunidadesEncerradas());
        assertEquals(24L, resposta.totalVoluntariosInscritos());
    }

    @Test
    void naoBuscaIndicadoresQuandoOngNaoExiste() {
        when(ongRepository.findByUsuarioId(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.buscarResumoPainel(7));

        verify(oportunidadeRepository, never()).countByOngUsuarioId(7);
    }
}
