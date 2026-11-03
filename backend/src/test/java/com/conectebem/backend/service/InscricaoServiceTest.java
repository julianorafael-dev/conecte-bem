package com.conectebem.backend.service;

import com.conectebem.backend.dto.InscricaoRequestDTO;
import com.conectebem.backend.exception.InscricaoConflictException;
import com.conectebem.backend.model.Inscricao;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.model.Oportunidade;
import com.conectebem.backend.model.StatusInscricao;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.InscricaoRepository;
import com.conectebem.backend.repository.OngRepository;
import com.conectebem.backend.repository.OportunidadeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InscricaoServiceTest {

    private final InscricaoRepository inscricaoRepository = mock(InscricaoRepository.class);
    private final OportunidadeRepository oportunidadeRepository = mock(OportunidadeRepository.class);
    private final OngRepository ongRepository = mock(OngRepository.class);
    private final InscricaoService service = new InscricaoService(
            inscricaoRepository,
            oportunidadeRepository,
            ongRepository
    );

    @Test
    void criaInscricaoPendenteQuandoHaVagaDisponivel() {
        Usuario voluntario = voluntario(1);
        Oportunidade oportunidade = oportunidade(10, 2, "ABERTA", 50);

        when(oportunidadeRepository.findByIdForUpdate(10)).thenReturn(Optional.of(oportunidade));
        when(inscricaoRepository.findByUsuarioIdAndOportunidadeId(1, 10)).thenReturn(Optional.empty());
        when(inscricaoRepository.countByOportunidadeIdAndStatusIn(any(), any())).thenReturn(1L);
        when(inscricaoRepository.save(any(Inscricao.class))).thenAnswer(invocation -> {
            Inscricao inscricao = invocation.getArgument(0);
            inscricao.setId(100);
            return inscricao;
        });

        var resposta = service.inscrever(new InscricaoRequestDTO(10), voluntario);

        assertEquals(100, resposta.id());
        assertEquals(StatusInscricao.PENDENTE, resposta.statusInscricao());
        assertEquals(10, resposta.oportunidadeId());
        assertNotNull(resposta.dataInscricao());
    }

    @Test
    void reativaInscricaoCanceladaSemCriarSegundoRegistro() {
        Usuario voluntario = voluntario(1);
        Oportunidade oportunidade = oportunidade(10, 2, "ABERTA", 50);
        Inscricao cancelada = inscricao(100, voluntario, oportunidade, StatusInscricao.CANCELADA);

        when(oportunidadeRepository.findByIdForUpdate(10)).thenReturn(Optional.of(oportunidade));
        when(inscricaoRepository.findByUsuarioIdAndOportunidadeId(1, 10)).thenReturn(Optional.of(cancelada));
        when(inscricaoRepository.countByOportunidadeIdAndStatusIn(any(), any())).thenReturn(1L);
        when(inscricaoRepository.save(cancelada)).thenReturn(cancelada);

        var resposta = service.inscrever(new InscricaoRequestDTO(10), voluntario);

        assertEquals(100, resposta.id());
        assertEquals(StatusInscricao.PENDENTE, cancelada.getStatus());
        assertEquals(StatusInscricao.PENDENTE, resposta.statusInscricao());
        assertNotNull(cancelada.getDataInscricao());
        verify(inscricaoRepository).save(cancelada);
    }

    @Test
    void impedeSegundaInscricaoAtivaNaMesmaOportunidade() {
        Usuario voluntario = voluntario(1);
        Oportunidade oportunidade = oportunidade(10, 2, "ABERTA", 50);
        Inscricao pendente = inscricao(100, voluntario, oportunidade, StatusInscricao.PENDENTE);

        when(oportunidadeRepository.findByIdForUpdate(10)).thenReturn(Optional.of(oportunidade));
        when(inscricaoRepository.findByUsuarioIdAndOportunidadeId(1, 10)).thenReturn(Optional.of(pendente));

        InscricaoConflictException excecao = assertThrows(
                InscricaoConflictException.class,
                () -> service.inscrever(new InscricaoRequestDTO(10), voluntario)
        );

        assertEquals("Você já possui uma inscrição ativa nesta oportunidade.", excecao.getMessage());
        verify(inscricaoRepository, never()).save(any());
    }

    @Test
    void impedeInscricaoQuandoCapacidadeFoiAtingida() {
        Usuario voluntario = voluntario(1);
        Oportunidade oportunidade = oportunidade(10, 2, "ABERTA", 50);

        when(oportunidadeRepository.findByIdForUpdate(10)).thenReturn(Optional.of(oportunidade));
        when(inscricaoRepository.findByUsuarioIdAndOportunidadeId(1, 10)).thenReturn(Optional.empty());
        when(inscricaoRepository.countByOportunidadeIdAndStatusIn(any(), any())).thenReturn(2L);

        InscricaoConflictException excecao = assertThrows(
                InscricaoConflictException.class,
                () -> service.inscrever(new InscricaoRequestDTO(10), voluntario)
        );

        assertEquals("Não há vagas disponíveis nesta oportunidade.", excecao.getMessage());
        verify(inscricaoRepository, never()).save(any());
    }

    @Test
    void impedeInscricaoEmOportunidadeEncerrada() {
        Usuario voluntario = voluntario(1);
        Oportunidade oportunidade = oportunidade(10, 2, "ENCERRADA", 50);

        when(oportunidadeRepository.findByIdForUpdate(10)).thenReturn(Optional.of(oportunidade));

        InscricaoConflictException excecao = assertThrows(
                InscricaoConflictException.class,
                () -> service.inscrever(new InscricaoRequestDTO(10), voluntario)
        );

        assertEquals("Esta oportunidade não está aberta para inscrições.", excecao.getMessage());
        verify(inscricaoRepository, never()).save(any());
    }

    @Test
    void impedeOngDeSeInscrever() {
        Usuario ong = usuario(2, TipoUsuario.ONG);

        AccessDeniedException excecao = assertThrows(
                AccessDeniedException.class,
                () -> service.inscrever(new InscricaoRequestDTO(10), ong)
        );

        assertEquals("Apenas contas do tipo voluntário podem realizar esta ação.", excecao.getMessage());
        verify(oportunidadeRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void impedeVoluntarioDeCancelarInscricaoDeOutraPessoa() {
        Usuario donoDaInscricao = voluntario(1);
        Usuario outroVoluntario = voluntario(2);
        Inscricao inscricao = inscricao(
                100,
                donoDaInscricao,
                oportunidade(10, 2, "ABERTA", 50),
                StatusInscricao.PENDENTE
        );

        when(inscricaoRepository.findById(100)).thenReturn(Optional.of(inscricao));

        AccessDeniedException excecao = assertThrows(
                AccessDeniedException.class,
                () -> service.cancelar(100, outroVoluntario)
        );

        assertEquals("Você só pode cancelar suas próprias inscrições.", excecao.getMessage());
        verify(inscricaoRepository, never()).save(any());
    }

    @Test
    void cancelaInscricaoDoProprioVoluntario() {
        Usuario voluntario = voluntario(1);
        Inscricao inscricao = inscricao(
                100,
                voluntario,
                oportunidade(10, 2, "ABERTA", 50),
                StatusInscricao.PENDENTE
        );

        when(inscricaoRepository.findById(100)).thenReturn(Optional.of(inscricao));
        when(inscricaoRepository.save(inscricao)).thenReturn(inscricao);

        service.cancelar(100, voluntario);

        assertEquals(StatusInscricao.CANCELADA, inscricao.getStatus());
        verify(inscricaoRepository).save(inscricao);
    }

    @Test
    void listaInscritosSomenteParaOngProprietaria() {
        Usuario usuarioOng = usuario(9, TipoUsuario.ONG);
        Ong ong = ongComId(50);
        Ong outraOng = ongComId(51);
        Oportunidade oportunidade = oportunidade(10, 2, "ABERTA", 51);
        oportunidade.setOng(outraOng);

        when(ongRepository.findByUsuarioId(9)).thenReturn(Optional.of(ong));
        when(oportunidadeRepository.findById(10)).thenReturn(Optional.of(oportunidade));

        AccessDeniedException excecao = assertThrows(
                AccessDeniedException.class,
                () -> service.listarInscritosDaOportunidade(10, usuarioOng)
        );

        assertEquals("Você só pode consultar inscritos das oportunidades da sua ONG.", excecao.getMessage());
        verify(inscricaoRepository, never()).findByOportunidadeIdOrderByDataInscricaoDesc(any());
    }

    @Test
    void listaInscritosParaOngProprietaria() {
        Usuario usuarioOng = usuario(9, TipoUsuario.ONG);
        Ong ong = ongComId(50);
        Oportunidade oportunidade = oportunidade(10, 2, "ABERTA", 50);
        Usuario voluntario = voluntario(1);
        Inscricao inscricao = inscricao(100, voluntario, oportunidade, StatusInscricao.PENDENTE);

        when(ongRepository.findByUsuarioId(9)).thenReturn(Optional.of(ong));
        when(oportunidadeRepository.findById(10)).thenReturn(Optional.of(oportunidade));
        when(inscricaoRepository.findByOportunidadeIdOrderByDataInscricaoDesc(10))
                .thenReturn(List.of(inscricao));

        var respostas = service.listarInscritosDaOportunidade(10, usuarioOng);

        assertEquals(1, respostas.size());
        assertEquals("Usuário de teste", respostas.getFirst().nomeVoluntario());
        assertEquals("teste@conectebem.local", respostas.getFirst().emailVoluntario());
        assertEquals(StatusInscricao.PENDENTE, respostas.getFirst().status());
    }

    @Test
    void listaCandidaturasDoVoluntarioAutenticado() {
        Usuario voluntario = voluntario(1);
        Oportunidade oportunidade = oportunidade(10, 2, "ABERTA", 50);
        Inscricao inscricao = inscricao(100, voluntario, oportunidade, StatusInscricao.PENDENTE);

        when(inscricaoRepository.findByUsuarioIdOrderByDataInscricaoDesc(1))
                .thenReturn(List.of(inscricao));

        var respostas = service.listarMinhas(voluntario);

        assertEquals(1, respostas.size());
        assertEquals("Oportunidade de teste", respostas.getFirst().tituloOportunidade());
        assertEquals(StatusInscricao.PENDENTE, respostas.getFirst().statusInscricao());
    }

    private Usuario voluntario(Integer id) {
        return usuario(id, TipoUsuario.VOLUNTARIO);
    }

    private Usuario usuario(Integer id, TipoUsuario tipo) {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(id);
        when(usuario.getTipo()).thenReturn(tipo);
        when(usuario.getNome()).thenReturn("Usuário de teste");
        when(usuario.getEmail()).thenReturn("teste@conectebem.local");
        return usuario;
    }

    private Oportunidade oportunidade(Integer id, Integer vagas, String status, Integer ongId) {
        Ong ong = ongComId(ongId);

        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setId(id);
        oportunidade.setOng(ong);
        oportunidade.setTitulo("Oportunidade de teste");
        oportunidade.setStatus(status);
        oportunidade.setVagas(vagas);
        return oportunidade;
    }

    private Ong ongComId(Integer id) {
        Ong ong = mock(Ong.class);
        when(ong.getId()).thenReturn(id);
        when(ong.getNome()).thenReturn("ONG de teste");
        return ong;
    }

    private Inscricao inscricao(
            Integer id,
            Usuario usuario,
            Oportunidade oportunidade,
            StatusInscricao status
    ) {
        Inscricao inscricao = new Inscricao();
        inscricao.setId(id);
        inscricao.setUsuario(usuario);
        inscricao.setOportunidade(oportunidade);
        inscricao.setStatus(status);
        inscricao.setDataInscricao(LocalDateTime.now().minusDays(1));
        return inscricao;
    }
}
