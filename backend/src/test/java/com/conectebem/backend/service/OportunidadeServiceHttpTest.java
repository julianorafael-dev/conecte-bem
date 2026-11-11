package com.conectebem.backend.service;

import com.conectebem.backend.controller.OportunidadeController;
import com.conectebem.backend.exception.GlobalExceptionHandler;
import com.conectebem.backend.model.Categoria;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.model.Oportunidade;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.CategoriaRepository;
import com.conectebem.backend.repository.InscricaoRepository;
import com.conectebem.backend.repository.OngRepository;
import com.conectebem.backend.repository.OportunidadeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes HTTP (MockMvc standalone) do OportunidadeController focados na
 * segurança da Etapa 2. NÃO inicia contexto Spring (nem Spring Boot) e NÃO
 * usa banco de dados: o {@link OportunidadeService} real é construído com
 * repositórios mockados (padrão do {@link OportunidadeServiceTest}) e o
 * {@link GlobalExceptionHandler} real converte as exceções em respostas HTTP
 * (403 JSON, 404 JSON, 400 JSON).
 */
class OportunidadeServiceHttpTest {

    @Test
    void metodoNaoSuportadoRetorna405ComMetodosPermitidos() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/oportunidades").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.erro").value("Method Not Allowed"))
                .andExpect(jsonPath("$.mensagem").value("Método HTTP não permitido para este endereço."))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Allow", org.hamcrest.Matchers.containsString("GET")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Allow", org.hamcrest.Matchers.containsString("POST")));
        org.mockito.Mockito.verifyNoInteractions(oportunidadeRepository, categoriaRepository, ongRepository);
    }

    private final OportunidadeRepository oportunidadeRepository = mock(OportunidadeRepository.class);
    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final OngRepository ongRepository = mock(OngRepository.class);
    private final InscricaoRepository inscricaoRepository = mock(InscricaoRepository.class);

    private final OportunidadeService service = new OportunidadeService(
            oportunidadeRepository, categoriaRepository, ongRepository, inscricaoRepository,
            new OportunidadeResponseMapper(inscricaoRepository));

    private MockMvc mockMvc;

    /** Corpo válido; ongId=999 é proposital: o service deve ignorá-lo. */
    private final String corpoValido = """
            {"ongId":999,"categoriaId":1,"titulo":"Título da oportunidade","descricao":"Descrição",
             "data":"2027-01-10","horario":"14:00","cidade":"Porto Alegre","estado":"RS",
             "vagas":5,"status":"ABERTA"}
            """;

    @BeforeEach
    void preparaMockMvc() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OportunidadeController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                // No standalone o resolver do Spring Security não é registrado por padrão;
                // sem ele o parâmetro @AuthenticationPrincipal não resolve.
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void limpaContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    // ---------- Ajudantes ----------

    /** Publica o usuário autenticado no SecurityContextHolder (mesma thread da requisição). */
    private void autenticado(TipoUsuario tipo, Integer usuarioId) {
        Usuario usuario = Usuario.builder()
                .id(usuarioId).nome("Usuário " + usuarioId).email("u" + usuarioId + "@example.com")
                .senha("hash-existente").tipo(tipo).build();

        var autenticacao = new UsernamePasswordAuthenticationToken(
                usuario, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + tipo.name())));

        SecurityContextHolder.getContext().setAuthentication(autenticacao);
    }

    /** Ong não expõe setId; o id é informado por mock, como em OportunidadeServiceTest. */
    private Ong ongComId(int id) {
        Ong ong = mock(Ong.class);
        when(ong.getId()).thenReturn(id);
        when(ong.getNome()).thenReturn("ONG " + id);
        return ong;
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

    private Categoria categoria() {
        Categoria categoria = new Categoria();
        categoria.setId(1);
        categoria.setNome("Doação");
        categoria.setDescricao("Ajudar o próximo");
        return categoria;
    }

    /** Faz o repositório devolver a própria entidade com id ao salvar. */
    private void repositorioSalvaComNovoId() {
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenAnswer(invocacao -> {
            Oportunidade salvo = invocacao.getArgument(0);
            salvo.setId(99);
            return salvo;
        });
    }

    private void preparaOngDoUsuario(Integer usuarioId, Ong ongDoUsuario) {
        when(ongRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(ongDoUsuario));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
    }

    // ---------- Cenário 1: ONG autenticada cria -> 201 ----------

    @Test
    void ongCriaOportunidadeRetorna201() throws Exception {
        preparaOngDoUsuario(7, ongComId(10));
        repositorioSalvaComNovoId();
        autenticado(TipoUsuario.ONG, 7);

        mockMvc.perform(post("/oportunidades")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoValido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.ongId").value(10));

        verify(oportunidadeRepository).save(any(Oportunidade.class));
    }

    // ---------- Cenário 1b: ongId no corpo é ignorado ----------

    @Test
    void ongCriaComCorpoInformandoOutraOngRetorna201ComPropriaOng() throws Exception {
        preparaOngDoUsuario(7, ongComId(10));
        repositorioSalvaComNovoId();
        autenticado(TipoUsuario.ONG, 7);

        mockMvc.perform(post("/oportunidades")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoValido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ongId").value(10));

        ArgumentCaptor<Oportunidade> captor = ArgumentCaptor.forClass(Oportunidade.class);
        verify(oportunidadeRepository).save(captor.capture());
        assertEquals(10, captor.getValue().getOng().getId(),
                "A oportunidade criada deve nascer da ONG do usuário autenticado (ongId do corpo ignorado).");
        verify(ongRepository, never()).findById(999);
    }

    // ---------- Cenário 2: ONG autenticada atualiza sua oportunidade -> 200 ----------

    @Test
    void ongAtualizaPropriaOportunidadeRetorna200() throws Exception {
        Ong ongA = ongComId(10);
        Oportunidade existente = oportunidadeDaOng(ongA, 5);
        when(oportunidadeRepository.findByIdForUpdate(5)).thenReturn(Optional.of(existente));
        when(oportunidadeRepository.save(existente)).thenReturn(existente);
        preparaOngDoUsuario(7, ongA);
        autenticado(TipoUsuario.ONG, 7);

        mockMvc.perform(put("/oportunidades/5")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoValido))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.ongId").value(10))
                .andExpect(jsonPath("$.titulo").value("Título da oportunidade"));
    }

    // ---------- Cenário 2b: reduzir vagas abaixo das inscrições ativas -> 409 ----------

    @Test
    void ongReduzVagasAbaixoDasInscricoesAtivasRetorna409() throws Exception {
        Ong ongA = ongComId(10);
        Oportunidade existente = oportunidadeDaOng(ongA, 5); // 5 vagas
        when(oportunidadeRepository.findByIdForUpdate(5)).thenReturn(Optional.of(existente));
        when(inscricaoRepository.countByOportunidadeIdAndStatusIn(any(), any())).thenReturn(3L);
        preparaOngDoUsuario(7, ongA);
        autenticado(TipoUsuario.ONG, 7);

        String corpoComVagasReduzidas = corpoValido.replace("\"vagas\":5", "\"vagas\":2");

        mockMvc.perform(put("/oportunidades/5")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoComVagasReduzidas))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem")
                        .value("Não é possível reduzir as vagas para 2: já existem 3 inscrições ativas."));

        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
        assertEquals(5, existente.getVagas(), "As vagas originais não podem ser alteradas.");
    }

    // ---------- Cenário 3: ONG autenticada exclui sua oportunidade -> 204 ----------

    @Test
    void ongExcluiPropriaOportunidadeRetorna204() throws Exception {
        Ong ongA = ongComId(10);
        Oportunidade existente = oportunidadeDaOng(ongA, 5);
        when(oportunidadeRepository.findByIdForUpdate(5)).thenReturn(Optional.of(existente));
        preparaOngDoUsuario(7, ongA);
        autenticado(TipoUsuario.ONG, 7);

        mockMvc.perform(delete("/oportunidades/5"))
                .andExpect(status().isNoContent());

        assertEquals("CANCELADA", existente.getStatus());
        verify(oportunidadeRepository).save(existente);
        verify(oportunidadeRepository, never()).deleteById(5);
    }

    // ---------- Cenário 4: ONG B tenta editar oportunidade da ONG A -> 403 com JSON ----------

    @Test
    void ongBEditaOportunidadeDaOngARetorna403ComJson() throws Exception {
        Ong ongA = ongComId(10);
        when(oportunidadeRepository.findByIdForUpdate(5)).thenReturn(Optional.of(oportunidadeDaOng(ongA, 5)));
        preparaOngDoUsuario(9, ongComId(20));
        autenticado(TipoUsuario.ONG, 9);

        mockMvc.perform(put("/oportunidades/5")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoValido))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem")
                        .value("Você só pode alterar oportunidades da sua própria ONG."));

        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
    }

    // ---------- Cenário 5: ONG B tenta excluir oportunidade da ONG A -> 403 com JSON ----------

    @Test
    void ongBExcluiOportunidadeDaOngARetorna403ComJson() throws Exception {
        Ong ongA = ongComId(10);
        when(oportunidadeRepository.findByIdForUpdate(5)).thenReturn(Optional.of(oportunidadeDaOng(ongA, 5)));
        preparaOngDoUsuario(9, ongComId(20));
        autenticado(TipoUsuario.ONG, 9);

        mockMvc.perform(delete("/oportunidades/5"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem")
                        .value("Você só pode excluir oportunidades da sua própria ONG."));

        verify(oportunidadeRepository, never()).deleteById(5);
    }

    // ---------- Cenário 6: edição de oportunidade inexistente -> 404 ----------

    @Test
    void edicaoDeOportunidadeInexistenteRetorna404() throws Exception {
        when(oportunidadeRepository.findByIdForUpdate(999)).thenReturn(Optional.empty());
        preparaOngDoUsuario(7, ongComId(10));
        autenticado(TipoUsuario.ONG, 7);

        mockMvc.perform(put("/oportunidades/999")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoValido))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Oportunidade não encontrada. ID: 999"));

        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
    }

    // ---------- Cenário 7: exclusão de oportunidade inexistente -> 404 ----------

    @Test
    void exclusaoDeOportunidadeInexistenteRetorna404() throws Exception {
        when(oportunidadeRepository.findById(999)).thenReturn(Optional.empty());
        preparaOngDoUsuario(7, ongComId(10));
        autenticado(TipoUsuario.ONG, 7);

        mockMvc.perform(delete("/oportunidades/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Oportunidade não encontrada. ID: 999"));

        verify(oportunidadeRepository, never()).deleteById(999);
    }

    // ---------- Cenário 8: VOLUNTARIO tenta criar -> 403 ----------

    @Test
    void voluntarioTentaCriarOportunidadeRetorna403() throws Exception {
        autenticado(TipoUsuario.VOLUNTARIO, 3);

        mockMvc.perform(post("/oportunidades")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoValido))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem")
                        .value("Apenas contas do tipo ONG podem gerenciar oportunidades."));

        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
    }

    // ---------- Cenário 9: corpo inválido na criação -> 400 ----------

    @Test
    void corpoInvalidoNaCriacaoRetorna400() throws Exception {
        autenticado(TipoUsuario.ONG, 7);

        String corpoInvalido = """
                {"ongId":10,"titulo":"Faltam campos obrigatórios"}
                """;

        mockMvc.perform(post("/oportunidades")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void statusInvalidoNaCriacaoRetorna400() throws Exception {
        autenticado(TipoUsuario.ONG, 7);

        String corpoComStatusInvalido = corpoValido.replace("\"ABERTA\"", "\"PAUSADA\"");

        mockMvc.perform(post("/oportunidades")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoComStatusInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem")
                        .value("status: O status deve ser ABERTA, ENCERRADA ou CANCELADA."));

        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
    }

    @Test
    void tituloComMaisDe150CaracteresRetorna400() throws Exception {
        autenticado(TipoUsuario.ONG, 7);

        String tituloInvalido = "A".repeat(151);
        String corpoComTituloInvalido = corpoValido.replace(
                "Título da oportunidade", tituloInvalido
        );

        mockMvc.perform(post("/oportunidades")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoComTituloInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem")
                        .value("titulo: O título deve ter no máximo 150 caracteres."));

        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
    }

    @Test
    void filtroComStatusInvalidoRetorna400() throws Exception {
        mockMvc.perform(get("/oportunidades").param("status", "PAUSADA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem")
                        .value("O status deve ser ABERTA, ENCERRADA ou CANCELADA."));
    }

    // ---------- Regressões: entrada HTTP inválida deve retornar 400 ----------

    @Test
    void jsonMalformadoRetorna400ComMensagemEstavel() throws Exception {
        autenticado(TipoUsuario.ONG, 7);

        mockMvc.perform(post("/oportunidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"JSON sem fechamento\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem")
                        .value("Corpo da requisição inválido. Verifique o JSON e os formatos dos campos."));

        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
    }

    @Test
    void dataEmFormatoInvalidoRetorna400ComMensagemEstavel() throws Exception {
        autenticado(TipoUsuario.ONG, 7);

        String corpoComDataInvalida = corpoValido.replace("2027-01-10", "10/01/2027");

        mockMvc.perform(post("/oportunidades")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoComDataInvalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem")
                        .value("Corpo da requisição inválido. Verifique o JSON e os formatos dos campos."));

        verify(oportunidadeRepository, never()).save(any(Oportunidade.class));
    }

    @Test
    void idNaoNumericoRetorna400ComMensagemEstavel() throws Exception {
        mockMvc.perform(get("/oportunidades/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Parâmetro 'id' deve ter um valor válido."));
    }
}
