package com.conectebem.backend.service;

import com.conectebem.backend.dto.OportunidadeRequestDTO;
import com.conectebem.backend.dto.OportunidadeResponseDTO;
import com.conectebem.backend.exception.ResourceNotFoundException;
import com.conectebem.backend.model.Categoria;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.model.Oportunidade;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.CategoriaRepository;
import com.conectebem.backend.repository.OngRepository;
import com.conectebem.backend.repository.OportunidadeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do OportunidadeService focados em segurança da Etapa 2:
 * resolução da ONG pelo usuário autenticado e validação de propriedade
 * na edição e exclusão. Não usa banco de dados (PostgreSQL ou H2).
 */
class OportunidadeServiceTest {

    private final OportunidadeRepository oportunidadeRepository = mock(OportunidadeRepository.class);
    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final OngRepository ongRepository = mock(OngRepository.class);
    private final OportunidadeService service =
            new OportunidadeService(oportunidadeRepository, categoriaRepository, ongRepository);

    // ---------- Ajudantes ----------

    /** Ong não expõe setId; o id é informado por mock, como os repositórios em OngServiceTest. */
    private Ong ongComId(int id) {
        Ong ong = mock(Ong.class);
        when(ong.getId()).thenReturn(id);
        when(ong.getNome()).thenReturn("ONG " + id);
        return ong;
    }

    private Usuario usuario(TipoUsuario tipo, Integer id) {
        return Usuario.builder()
                .id(id).nome("Usuário " + id).email("u" + id + "@example.com")
                .senha("hash-existente").tipo(tipo).build();
    }

    private OportunidadeRequestDTO dtoComOngId(Integer ongId) {
        return new OportunidadeRequestDTO(
                ongId, 1, "Título da oportunidade", "Descrição",
                LocalDate.of(2027, 1, 10), LocalTime.of(14, 0),
                "Porto Alegre", "RS", 5, "ABERTA");
    }

    private Categoria categoria() {
        Categoria categoria = new Categoria();
        categoria.setId(1);
        categoria.setNome("Doação");
        categoria.setDescricao("Ajudar o próximo");
        return categoria;
    }

    private Oportunidade oportunidadeDaOng(Ong ong, Integer id) {
        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setId(id);
        oportunidade.setOng(ong);
        oportunidade.setCategoria(categoria());
        oportunidade.setTitulo("Título original");
        oportunidade.setDescricao("Descrição original");
        oportunidade.setData(LocalDate.of(2027, 1, 10));
        oportunidade.setHorario(LocalTime.of(14, 0));
        oportunidade.setCidade("Porto Alegre");
        oportunidade.setEstado("RS");
        oportunidade.setVagas(5);
        oportunidade.setStatus("ABERTA");
        oportunidade.setCriadoEm(LocalDateTime.of(2026, 10, 1, 8, 0));
        return oportunidade;
    }

    /** Faz o repositório devolver a própria entidade com id ao salvar. */
    private void repositorioSalvaComNovoId() {
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenAnswer(invocacao -> {
            Oportunidade salvo = invocacao.getArgument(0);
            salvo.setId(99);
            return salvo;
        });
    }

    // ---------- Cenário 1: criação associa a ONG do usuário ----------

    @Test
    void associaCriacaoAongLocalizadaPeloUsuarioId() {
        Ong ongA = ongComId(10);
        when(ongRepository.findByUsuarioId(7)).thenReturn(Optional.of(ongA));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        repositorioSalvaComNovoId();

        OportunidadeResponseDTO resposta =
                service.criar(dtoComOngId(10), usuario(TipoUsuario.ONG, 7));

        assertEquals(10, resposta.ongId());
        assertEquals("ONG 10", resposta.nomeOng());
        assertEquals(99, resposta.id());
    }

    // ---------- Cenário 2: ongId do corpo é ignorado ----------

    @Test
    void associaOngeMesmoQuandoOngIdNoCorpoForDiferente() {
        Ong ongA = ongComId(10);
        when(ongRepository.findByUsuarioId(7)).thenReturn(Optional.of(ongA));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        repositorioSalvaComNovoId();

        OportunidadeResponseDTO resposta =
                service.criar(dtoComOngId(999), usuario(TipoUsuario.ONG, 7));

        assertEquals(10, resposta.ongId(), "Deve usar a ONG do usuário autenticado, não a do corpo.");

        ArgumentCaptor<Oportunidade> captor = ArgumentCaptor.forClass(Oportunidade.class);
        verify(oportunidadeRepository).save(captor.capture());
        assertEquals(ongA, captor.getValue().getOng());
        verify(ongRepository, never()).findById(anyInt());
    }

    // ---------- Cenário 3: edição da própria oportunidade ----------

    @Test
    void editaOportunidadeDaPropriaOng() {
        Ong ongA = ongComId(10);
        Oportunidade existente = oportunidadeDaOng(ongA, 5);
        when(oportunidadeRepository.findById(5)).thenReturn(Optional.of(existente));
        when(ongRepository.findByUsuarioId(7)).thenReturn(Optional.of(ongA));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        when(oportunidadeRepository.save(existente)).thenReturn(existente);

        OportunidadeResponseDTO resposta =
                service.atualizar(5, dtoComOngId(999), usuario(TipoUsuario.ONG, 7));

        assertEquals("Título da oportunidade", resposta.titulo());
        assertEquals(10, resposta.ongId());

        verify(oportunidadeRepository).save(existente);
        assertEquals(ongA, existente.getOng(), "Edição não deve transferir a oportunidade de ONG.");
        assertEquals("Título da oportunidade", existente.getTitulo());
    }

    // ---------- Cenário 4: edição da oportunidade da outra ONG ----------

    @Test
    void negaEdicaoDeOportunidadeDeOutraOng() {
        Ong ongA = ongComId(10);
        Ong ongB = ongComId(20);
        Oportunidade existente = oportunidadeDaOng(ongA, 5);
        when(oportunidadeRepository.findById(5)).thenReturn(Optional.of(existente));
        when(ongRepository.findByUsuarioId(9)).thenReturn(Optional.of(ongB));

        AccessDeniedException excecao = assertThrows(AccessDeniedException.class,
                () -> service.atualizar(5, dtoComOngId(10), usuario(TipoUsuario.ONG, 9)));

        assertEquals("Você só pode alterar oportunidades da sua própria ONG.", excecao.getMessage());
        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
    }

    // ---------- Cenário 5: exclusão da oportunidade da outra ONG ----------

    @Test
    void negaExclusaoDeOportunidadeDeOutraOng() {
        Ong ongA = ongComId(10);
        Ong ongB = ongComId(20);
        Oportunidade existente = oportunidadeDaOng(ongA, 5);
        when(oportunidadeRepository.findById(5)).thenReturn(Optional.of(existente));
        when(ongRepository.findByUsuarioId(9)).thenReturn(Optional.of(ongB));

        AccessDeniedException excecao = assertThrows(AccessDeniedException.class,
                () -> service.excluir(5, usuario(TipoUsuario.ONG, 9)));

        assertEquals("Você só pode excluir oportunidades da sua própria ONG.", excecao.getMessage());
        verify(oportunidadeRepository, never()).deleteById(5);
    }

    // ---------- Cenário 6: usuário VOLUNTARIO ----------

    @Test
    void voluntarioNaoPodeCriarOportunidade() {
        AccessDeniedException excecao = assertThrows(AccessDeniedException.class,
                () -> service.criar(dtoComOngId(10), usuario(TipoUsuario.VOLUNTARIO, 3)));

        assertEquals("Apenas contas do tipo ONG podem gerenciar oportunidades.", excecao.getMessage());
        verifyNoInteractions(oportunidadeRepository, categoriaRepository, ongRepository);
    }

    @Test
    void voluntarioNaoPodeEditarOportunidade() {
        Ong ongA = ongComId(10);
        Oportunidade existente = oportunidadeDaOng(ongA, 5);
        when(oportunidadeRepository.findById(5)).thenReturn(Optional.of(existente));

        assertThrows(AccessDeniedException.class,
                () -> service.atualizar(5, dtoComOngId(10), usuario(TipoUsuario.VOLUNTARIO, 3)));

        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
    }

    @Test
    void voluntarioNaoPodeExcluirOportunidade() {
        Ong ongA = ongComId(10);
        Oportunidade existente = oportunidadeDaOng(ongA, 5);
        when(oportunidadeRepository.findById(5)).thenReturn(Optional.of(existente));

        assertThrows(AccessDeniedException.class,
                () -> service.excluir(5, usuario(TipoUsuario.VOLUNTARIO, 3)));

        verify(oportunidadeRepository, never()).deleteById(5);
    }

    // ---------- Cenário 7: oportunidade inexistente ----------

    @Test
    void edicaoDeOportunidadeInexistenteRetornaNotFound() {
        when(oportunidadeRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException excecao = assertThrows(ResourceNotFoundException.class,
                () -> service.atualizar(999, dtoComOngId(10), usuario(TipoUsuario.ONG, 7)));

        assertEquals("Oportunidade não encontrada. ID: 999", excecao.getMessage());
        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
    }

    @Test
    void exclusaoDeOportunidadeInexistenteRetornaNotFound() {
        when(oportunidadeRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException excecao = assertThrows(ResourceNotFoundException.class,
                () -> service.excluir(999, usuario(TipoUsuario.ONG, 7)));

        assertEquals("Oportunidade não encontrada. ID: 999", excecao.getMessage());
        verify(oportunidadeRepository, never()).deleteById(999);
    }

    // ---------- Cenário 8: conta ONG sem registro de ONG ----------

    @Test
    void contaOngSemRegistroCorrespondenteRetornaNotFound() {
        when(ongRepository.findByUsuarioId(7)).thenReturn(Optional.empty());

        ResourceNotFoundException excecao = assertThrows(ResourceNotFoundException.class,
                () -> service.criar(dtoComOngId(10), usuario(TipoUsuario.ONG, 7)));

        assertEquals("ONG do usuário autenticado não encontrada.", excecao.getMessage());
        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
    }
}
