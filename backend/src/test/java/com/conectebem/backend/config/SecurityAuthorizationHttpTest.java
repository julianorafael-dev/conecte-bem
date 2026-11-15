package com.conectebem.backend.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.UsuarioRepository;
import com.conectebem.backend.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Servidor real: também exercita o despacho de erro que não acontece no MockMvc. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SecurityAuthorizationHttpTest {

    @Test
    void metodoNaoSuportadoRetorna405SemEncerrarAutenticacao() throws Exception {
        String token = jwt.gerarToken(criar(TipoUsuario.VOLUNTARIO));
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/voluntarios/me"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + token)
                .method("PATCH", HttpRequest.BodyPublishers.noBody()).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode());
        assertTrue(response.body().contains("\"status\":405"));
        assertTrue(response.headers().firstValue("Allow").orElse("").contains("GET"));
        assertTrue(response.headers().firstValue("Allow").orElse("").contains("PUT"));
        assertEquals(200, get("/voluntarios/me", token).statusCode());
    }
    @Value("${local.server.port}")
    private int port;
    @Value("${api.security.token.secret}")
    private String secret;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private JwtService jwt;
    private HttpClient client;
    private final List<Integer> criados = new ArrayList<>();

    @BeforeEach
    void iniciar() {
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @AfterEach
    void limpar() { usuarios.deleteAllById(criados); }

    private Usuario criar(TipoUsuario tipo) {
        Usuario usuario = usuarios.save(Usuario.builder().nome("QA autorização")
                .email(UUID.randomUUID() + "@example.com").senha("hash-de-teste")
                .tipo(tipo).build());
        criados.add(usuario.getId());
        return usuario;
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(15));
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void voluntarioRecebe403EContinuaAutenticado() throws Exception {
        String token = jwt.gerarToken(criar(TipoUsuario.VOLUNTARIO));
        var proibida = get("/ongs/me/painel", token);
        assertEquals(403, proibida.statusCode());
        assertTrue(proibida.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        assertTrue(proibida.body().contains("\"status\":403"));
        assertTrue(proibida.body().contains("permissão"));
        assertEquals(200, get("/voluntarios/me", token).statusCode());
    }

    @Test
    void ongRecebe403EmInscricoesDeVoluntarioSemInvalidarToken() throws Exception {
        Usuario usuario = criar(TipoUsuario.ONG);
        String token = jwt.gerarToken(usuario);
        assertEquals(403, get("/inscricoes/me", token).statusCode());
        assertEquals(usuario.getEmail(), jwt.validarToken(token));
        // Conta ONG sem cadastro complementar: 404 do serviço, não 401/403 do filtro.
        assertEquals(404, get("/ongs/me", token).statusCode());
    }

    @Test
    void semTokenOuComTokenInvalidoRecebe401() throws Exception {
        for (String token : new String[]{null, "token-invalido"}) {
            var response = get("/voluntarios/me", token);
            assertEquals(401, response.statusCode());
            assertTrue(response.body().contains("\"status\":401"));
        }
    }

    @Test
    void tokenExpiradoRecebe401() throws Exception {
        Usuario usuario = criar(TipoUsuario.VOLUNTARIO);
        String token = JWT.create().withIssuer("conecte-bem-api").withSubject(usuario.getEmail())
                .withExpiresAt(Instant.now().minusSeconds(60)).sign(Algorithm.HMAC256(secret));
        assertEquals(401, get("/voluntarios/me", token).statusCode());
    }
}
