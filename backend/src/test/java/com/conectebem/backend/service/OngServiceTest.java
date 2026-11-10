package com.conectebem.backend.service;

import com.conectebem.backend.exception.ResourceNotFoundException;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.dto.OngRequestDTO;
import org.springframework.security.access.AccessDeniedException;
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

    private final OngRequestDTO dados = new OngRequestDTO(7, "Nome atualizado", "11222333000181",
            "Descrição", null, "São Paulo", "SP");

    private Usuario usuario(TipoUsuario tipo, int id) {
        return Usuario.builder().id(id).tipo(tipo).build();
    }

    private Ong ongDoUsuario7() {
        Ong ong = new Ong();
        ong.setUsuarioId(7);
        ong.setNome("Nome original");
        when(ongRepository.findById(10)).thenReturn(Optional.of(ong));
        return ong;
    }

    @Test
    void voluntarioNaoAlteraMesmoInformandoIdDoDonoNoCorpo() {
        assertThrows(AccessDeniedException.class,
                () -> service.atualizar(10, dados, usuario(TipoUsuario.VOLUNTARIO, 3)));
        org.mockito.Mockito.verifyNoInteractions(ongRepository);
    }

    @Test
    void outraOngNaoAlteraDadosDoProprietario() {
        Ong ong = ongDoUsuario7();
        assertThrows(AccessDeniedException.class,
                () -> service.atualizar(10, dados, usuario(TipoUsuario.ONG, 9)));
        assertEquals("Nome original", ong.getNome());
        verify(ongRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void proprietarioPodeAlterarSemTrocarVinculoPeloCorpo() {
        Ong ong = ongDoUsuario7();
        when(ongRepository.save(ong)).thenReturn(ong);
        OngRequestDTO corpoComOutroUsuario = new OngRequestDTO(999, dados.nome(), dados.cnpj(),
                dados.descricao(), dados.telefone(), dados.cidade(), dados.estado());
        var resposta = service.atualizar(10, corpoComOutroUsuario, usuario(TipoUsuario.ONG, 7));
        assertEquals("Nome atualizado", resposta.nome());
        assertEquals(7, ong.getUsuarioId());
        verify(ongRepository).save(ong);
    }

    @Test
    void usuarioAusenteNaoAltera() {
        assertThrows(AccessDeniedException.class, () -> service.atualizar(10, dados, null));
        org.mockito.Mockito.verifyNoInteractions(ongRepository);
    }

    @Test
    void ongInexistenteRetornaNaoEncontradoParaContaOng() {
        when(ongRepository.findById(10)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.atualizar(10, dados, usuario(TipoUsuario.ONG, 7)));
        verify(ongRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

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
