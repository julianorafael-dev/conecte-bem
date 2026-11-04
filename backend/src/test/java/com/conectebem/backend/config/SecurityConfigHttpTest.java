package com.conectebem.backend.config;

import com.conectebem.backend.controller.CategoriaController;
import com.conectebem.backend.controller.OngController;
import com.conectebem.backend.controller.OportunidadeController;
import com.conectebem.backend.dto.OportunidadeResponseDTO;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.UsuarioRepository;
import com.conectebem.backend.security.JwtAuthFilter;
import com.conectebem.backend.security.JwtService;
import com.conectebem.backend.service.CategoriaService;
import com.conectebem.backend.service.OngService;
import com.conectebem.backend.service.OportunidadeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa a CADEIA REAL do Spring Security (SecurityConfig + JwtAuthFilter) por
 * meio de um slice {@link WebMvcTest}, sem tocar em PostgreSQL, H2 ou no
 * contexto completo do Spring Boot.
 *
 * <p>O {@code SecurityConfig} e o {@code JwtAuthFilter} são importados
 * explicitamente; {@code JwtService} e {@code UsuarioRepository} são
 * mockados com {@code @MockitoBean} para fabricarem os usuários autenticados.
 * Os serviços dos controllers também são mockados: o foco aqui é a camada de
 * filtros/autorização, não o service (esse fica com as classes standalone).</p>
 */
@WebMvcTest(controllers = {
        OportunidadeController.class,
        OngController.class,
        CategoriaController.class
})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class SecurityConfigHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OportunidadeService oportunidadeService;

    @MockitoBean
    private OngService ongService;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    /**
     * O slice do @WebMvcTest pode não registrar o resolver do Spring Security;
     * registrar manualmente é inofensivo mesmo quando já registrado.
     */
    @org.springframework.boot.test.context.TestConfiguration
    static class ResolverAdicional {
        @Bean
        WebMvcConfigurer resolverDePrincipal() {
            return new WebMvcConfigurer() {
                @Override
                public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                    resolvers.add(new AuthenticationPrincipalArgumentResolver());
                }
            };
        }
    }

    // ---------- Ajudantes ----------

    private static final String CORPO_VALIDO = """
            {"ongId":10,"categoriaId":1,"titulo":"Título","descricao":"d",
             "data":"2027-01-10","horario":"14:00","cidade":"Porto Alegre","estado":"RS",
             "vagas":5,"status":"ABERTA"}
            """;

    private Usuario usuario(TipoUsuario tipo, Integer usuarioId) {
        return Usuario.builder()
                .id(usuarioId).nome("Usuário " + usuarioId)
                .email(tipo.name().toLowerCase() + usuarioId + "@example.com")
                .senha("hash-existente").tipo(tipo).build();
    }

    /** Simula um token válido na cadeia real: JwtService e UsuarioRepository mockados. */
    private void comTokenDe(TipoUsuario tipo, Integer usuarioId) {
        Usuario usuario = usuario(tipo, usuarioId);
        when(jwtService.validarToken("token-" + tipo.name()+ usuarioId)).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    }

    private static String bearer(TipoUsuario tipo, Integer usuarioId) {
        return "Bearer token-" + tipo.name() + usuarioId;
    }

    // ---------- Cenário 1: GET /oportunidades sem token -> 200 ----------

    @Test
    void getOportunidadesSemTokenRetorna200() throws Exception {
        when(oportunidadeService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/oportunidades"))
                .andExpect(status().isOk());
    }

    // ---------- Cenário 2: POST /oportunidades sem token -> 401 ----------

    @Test
    void postOportunidadesSemToken() throws Exception {
        mockMvc.perform(post("/oportunidades")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Cenários 3, 4 e 5: sem token (PUT / DELETE / GET /ongs/me/oportunidades) ----------

    @Test
    void putOportunidadesSemToken() throws Exception {
        mockMvc.perform(put("/oportunidades/5")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteOportunidadesSemToken() throws Exception {
        mockMvc.perform(delete("/oportunidades/5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMeOportunidadesSemToken() throws Exception {
        mockMvc.perform(get("/ongs/me/oportunidades"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Cenário 6: GET /ongs/me/oportunidades com VOLUNTARIO -> 403 ----------

    @Test
    void getMeOportunidadesComVoluntarioRetorna403() throws Exception {
        comTokenDe(TipoUsuario.VOLUNTARIO, 3);

        mockMvc.perform(get("/ongs/me/oportunidades")
                        .header("Authorization", bearer(TipoUsuario.VOLUNTARIO, 3)))
                .andExpect(status().isForbidden());
    }

    // ---------- Cenário 7: POST com ONG autenticado e service mockado -> 201 ----------

    @Test
    void postComUsuarioOngAutenticadoRetorna201() throws Exception {
        comTokenDe(TipoUsuario.ONG, 7);
        OportunidadeResponseDTO resposta = new OportunidadeResponseDTO(
                99, 10, "ONG 10", 1, "Doação", "Título", "d",
                LocalDate.of(2027, 1, 10), LocalTime.of(14, 0),
                "Porto Alegre", "RS", 5, "ABERTA",
                LocalDateTime.of(2026, 10, 30, 12, 0));
        when(oportunidadeService.criar(any(), any())).thenReturn(resposta);

        mockMvc.perform(post("/oportunidades")
                        .header("Authorization", bearer(TipoUsuario.ONG, 7))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ongId").value(10));
    }

    // ---------- Cenário 8: service lança AccessDeniedException -> 403 (JSON, não 500) ----------

    @Test
    void accessDeniedDoServiceRetorna403ComJson() throws Exception {
        comTokenDe(TipoUsuario.ONG, 7);
        when(oportunidadeService.criar(any(), any()))
                .thenThrow(new AccessDeniedException("Apenas contas do tipo ONG podem gerenciar oportunidades."));

        mockMvc.perform(post("/oportunidades")
                        .header("Authorization", bearer(TipoUsuario.ONG, 7))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem")
                        .value("Apenas contas do tipo ONG podem gerenciar oportunidades."));
    }

    // ---------- Cenário 9: GETs públicos sem token ----------

    @Test
    void getsPublicosSemTokenRetornam200() throws Exception {
        when(ongService.listar()).thenReturn(List.of());
        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(oportunidadeService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/oportunidades")).andExpect(status().isOk());
        mockMvc.perform(get("/ongs")).andExpect(status().isOk());
        mockMvc.perform(get("/categorias")).andExpect(status().isOk());
    }

    // ---------- Ponto 1 (defesa em profundidade): escritas só com papel ONG no filtro ----------

    /**
     * Com os novos matchers de escrita, o VOLUNTARIO autenticado é barrado no
     * FILTER (403 sem corpo JSON, diferente do 403 JSON do service) e o
     * OportunidadeService nunca é chamado.
     */
    @Test
    void voluntarioPostOportunidadesRetorna403NoFiltro() throws Exception {
        comTokenDe(TipoUsuario.VOLUNTARIO, 3);

        mockMvc.perform(post("/oportunidades")
                        .header("Authorization", bearer(TipoUsuario.VOLUNTARIO, 3))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isForbidden());

        verifyNoInteractions(oportunidadeService);
    }

    @Test
    void voluntarioPutOportunidadesRetorna403NoFiltro() throws Exception {
        comTokenDe(TipoUsuario.VOLUNTARIO, 3);

        mockMvc.perform(put("/oportunidades/5")
                        .header("Authorization", bearer(TipoUsuario.VOLUNTARIO, 3))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isForbidden());

        verifyNoInteractions(oportunidadeService);
    }

    @Test
    void voluntarioDeleteOportunidadesRetorna403NoFiltro() throws Exception {
        comTokenDe(TipoUsuario.VOLUNTARIO, 3);

        mockMvc.perform(delete("/oportunidades/5")
                        .header("Authorization", bearer(TipoUsuario.VOLUNTARIO, 3)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(oportunidadeService);
    }

    /** ONG autenticada continua passando pelo filtro e chegando ao controller/service. */
    @Test
    void ongPassaPeloFiltroNasEscritas() throws Exception {
        comTokenDe(TipoUsuario.ONG, 7);
        OportunidadeResponseDTO resposta = new OportunidadeResponseDTO(
                99, 10, "ONG 10", 1, "Doação", "Título", "d",
                LocalDate.of(2027, 1, 10), LocalTime.of(14, 0),
                "Porto Alegre", "RS", 5, "ABERTA",
                LocalDateTime.of(2026, 10, 30, 12, 0));
        when(oportunidadeService.criar(any(), any())).thenReturn(resposta);

        mockMvc.perform(post("/oportunidades")
                        .header("Authorization", bearer(TipoUsuario.ONG, 7))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated());

        verify(oportunidadeService).criar(any(), any());
    }

    // ---------- Reforço: token com e-mail inexistente -> sem autenticação ----------
    @Test
    void tokenComUsuarioInexistenteNaoAutentica() throws Exception {
        when(jwtService.validarToken("token-ong7")).thenReturn("fantasma@example.com");
        when(usuarioRepository.findByEmail("fantasma@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/oportunidades")
                        .header("Authorization", "Bearer token-ong7")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Reforço: /ongs/me/oportunidades com ONG autenticada atravessa a cadeia ----------

    @Test
    void getMeOportunidadesComOngAtravessaACadeia() throws Exception {
        comTokenDe(TipoUsuario.ONG, 7);
        when(ongService.listarOportunidadesDoUsuario(7)).thenReturn(List.of());

        mockMvc.perform(get("/ongs/me/oportunidades")
                        .header("Authorization", bearer(TipoUsuario.ONG, 7)))
                .andExpect(status().isOk());
    }
}
