package com.conectebem.backend.service;

import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.UsuarioRepository;
import com.conectebem.backend.repository.OngRepository;
import com.conectebem.backend.security.JwtService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Registro HTTP, persistência real em H2 e leitura do perfil pela cadeia JWT. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CadastroDadosHttpTest {
    @Test
    void cadastroLegadoBloqueiaTodosOsPerfisSemGravar() throws Exception {
        long quantidade = ongs.count();
        for (TipoUsuario tipo : TipoUsuario.values()) {
            Usuario usuario = usuarios.save(Usuario.builder().nome("QA legado").email(email()).senha("hash").tipo(tipo).build());
            mvc.perform(post("/ongs").header("Authorization", "Bearer " + jwt.gerarToken(usuario))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"usuarioId\":" + usuario.getId() + ",\"nome\":\"ONG indevida\",\"cnpj\":\"11222333000181\"}"))
                    .andExpect(status().isForbidden());
        }
        assertEquals(quantidade, ongs.count());
    }

    @Test
    void repositorioDetectaCnpjLegadoPontuadoENaoConfundeProprioCadastro() {
        Usuario usuario = usuarios.save(Usuario.builder().nome("QA CNPJ").email(email()).senha("hash").tipo(TipoUsuario.ONG).build());
        var ong = new com.conectebem.backend.model.Ong();
        ong.setUsuarioId(usuario.getId());
        ong.setNome("QA CNPJ");
        ong.setCnpj("11.222.333/0001-81");
        ong = ongs.saveAndFlush(ong);
        assertTrue(ongs.existsByCnpj("11222333000181"));
        assertFalse(ongs.existsByCnpjAndIdNot("11222333000181", ong.getId()));
        assertTrue(ongs.existsByCnpjAndIdNot("11222333000181", -1));
    }

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired OngRepository ongs;
    @Autowired EntityManager em;
    @Autowired JwtService jwt;

    private String email() { return UUID.randomUUID() + "@example.com"; }

    private String cadastro(String email, String tipo, String campos) {
        return """
                {"nome":"Pessoa de teste","email":"%s","senha":"teste123",
                 "tipo":"%s"%s}
                """.formatted(email, tipo, campos);
    }

    @Test
    void voluntarioPersisteDadosEConsultaSomenteSeuPerfil() throws Exception {
        String email = email();
        mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(cadastro(email, "VOLUNTARIO", """
                          ,"cpf":"529.982.247-25","dataNascimento":"1995-05-20","telefone":"11999990000"
                          """)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").doesNotExist())
                .andExpect(jsonPath("$.senha").doesNotExist());
        em.flush();
        em.clear();
        Usuario salvo = usuarios.findByEmail(email).orElseThrow();
        assertEquals("52998224725", salvo.getCpf());
        assertEquals(LocalDate.of(1995, 5, 20), salvo.getDataNascimento());
        assertEquals("11999990000", salvo.getTelefone());
        String token = jwt.gerarToken(salvo);
        mvc.perform(get("/voluntarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.dataNascimento").value("1995-05-20"))
                .andExpect(jsonPath("$.telefone").value("11999990000"))
                .andExpect(jsonPath("$.senha").doesNotExist());
        mvc.perform(put("/voluntarios/me").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Nome atualizado\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cpf").value("52998224725"));
        em.flush();
        em.clear();
        assertEquals(LocalDate.of(1995, 5, 20), usuarios.findByEmail(email).orElseThrow().getDataNascimento());
        mvc.perform(get("/voluntarios/me")).andExpect(status().isUnauthorized());
        Usuario outro = usuarios.save(Usuario.builder().nome("Outro").email(email()).senha("hash")
                .tipo(TipoUsuario.VOLUNTARIO).build());
        mvc.perform(get("/voluntarios/me").header("Authorization", "Bearer " + jwt.gerarToken(outro)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cpf").isEmpty());
    }

    @Test
    void ongPersisteDataCriacaoETelefone() throws Exception {
        String email = email();
        mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(cadastro(email, "ONG", """
                          ,"cnpj":"11222333000181","dataCriacao":"2010-02-03","telefone":"1133334444"
                          """)))
                .andExpect(status().isCreated());
        em.flush();
        em.clear();
        Usuario salvo = usuarios.findByEmail(email).orElseThrow();
        var ong = ongs.findByUsuarioId(salvo.getId()).orElseThrow();
        assertEquals(LocalDate.of(2010, 2, 3), ong.getDataCriacao());
        assertEquals("1133334444", ong.getTelefone());
        mvc.perform(get("/ongs/me").header("Authorization", "Bearer " + jwt.gerarToken(salvo)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.dataCriacao").value("2010-02-03"))
                .andExpect(jsonPath("$.telefone").value("1133334444"));
    }

    @Test
    void dadosOpcionaisAusentesMantemCompatibilidade() throws Exception {
        String email = email();
        mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(email, "VOLUNTARIO", ",\"dataNascimento\":\"1990-01-01\""))).andExpect(status().isCreated());
        em.flush();
        em.clear();
        Usuario salvo = usuarios.findByEmail(email).orElseThrow();
        assertNull(salvo.getCpf());
        assertEquals(LocalDate.of(1990, 1, 1), salvo.getDataNascimento());
        assertNull(salvo.getTelefone());
    }

    @Test
    void rejeitaCpfInvalidoDatasFuturasETelefoneLongoSemCriarConta() throws Exception {
        for (String campos : new String[]{
                ",\"cpf\":\"11111111111\"", ",\"cpf\":\"52998224724\"",
                ",\"dataNascimento\":\"" + LocalDate.now().plusDays(2) + "\"",
                ",\"dataNascimento\":\"data-invalida\"",
                ",\"telefone\":\"123456789012345678901\""}) {
            String email = email();
            mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                    .content(cadastro(email, "VOLUNTARIO", campos))).andExpect(status().isBadRequest());
            assertFalse(usuarios.existsByEmail(email));
        }
        String email = email();
        mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(email, "ONG", ",\"cnpj\":\"11222333000181\",\"dataCriacao\":\""
                        + LocalDate.now().plusDays(2) + "\""))).andExpect(status().isBadRequest());
        assertFalse(usuarios.existsByEmail(email));
    }

    @Test
    void idadeMinimaEObrigatoriaMesmoEmChamadaDireta() throws Exception {
        LocalDate limite = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).minusYears(18);
        for (String campos : new String[]{"", ",\"dataNascimento\":null",
                ",\"dataNascimento\":\"" + limite.plusDays(1) + "\""}) {
            String email = email();
            mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                    .content(cadastro(email, "VOLUNTARIO", campos))).andExpect(status().isBadRequest());
            assertFalse(usuarios.existsByEmail(email));
        }
        mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(email(), "VOLUNTARIO", ",\"dataNascimento\":\"" + limite + "\"")))
                .andExpect(status().isCreated());
        mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(email(), "ONG", ",\"cnpj\":\"11222333000181\",\"dataCriacao\":\""
                        + limite.plusYears(18) + "\""))).andExpect(status().isCreated());
    }
}
