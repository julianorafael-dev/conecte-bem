package com.conectebem.backend.security;

import com.auth0.jwt.JWT;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SEGREDO_TESTE = "segredo-ficticio-exclusivo-para-testes-12345";

    @Test
    void tokenExpiraDuasHorasAposOInstanteAtualIndependenteDoFusoHorario() {
        Instant instanteFixo = Instant.parse("2026-11-08T15:00:00Z");
        Clock relogioEmOutroFuso = Clock.fixed(instanteFixo, ZoneId.of("Asia/Tokyo"));
        JwtService service = new JwtService(SEGREDO_TESTE, relogioEmOutroFuso);

        String token = service.gerarToken(usuario());

        assertEquals(
                instanteFixo.plus(Duration.ofHours(2)),
                JWT.decode(token).getExpiresAtAsInstant()
        );
        assertEquals("voluntario@example.com", service.validarToken(token));
    }

    @Test
    void tokenExpiradoNaoEConsideradoValido() {
        Instant instanteFixo = Instant.parse("2026-11-08T15:00:00Z");
        JwtService service = new JwtService(SEGREDO_TESTE, Clock.fixed(instanteFixo, ZoneId.of("UTC")));
        String tokenExpirado = JWT.create()
                .withIssuer("conecte-bem-api")
                .withSubject("voluntario@example.com")
                .withExpiresAt(instanteFixo.minusSeconds(1))
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(SEGREDO_TESTE));

        assertNull(service.validarToken(tokenExpirado));
    }

    @Test
    void tokenDeixaDeSerValidoAoAtingirDuasHoras() {
        Instant inicio = Instant.parse("2026-11-08T15:00:00Z");
        JwtService emissor = new JwtService(SEGREDO_TESTE, Clock.fixed(inicio, ZoneId.of("UTC")));
        String token = emissor.gerarToken(usuario());
        JwtService antes = new JwtService(SEGREDO_TESTE,
                Clock.fixed(inicio.plusSeconds(7199), ZoneId.of("UTC")));
        JwtService noLimite = new JwtService(SEGREDO_TESTE,
                Clock.fixed(inicio.plusSeconds(7200), ZoneId.of("UTC")));

        assertEquals("voluntario@example.com", antes.validarToken(token));
        assertNull(noLimite.validarToken(token));
    }

    @Test
    void segredoAusenteOuCurtoERejeitado() {
        assertThrows(IllegalStateException.class, () -> new JwtService("curto", Clock.systemUTC()));
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(1)
                .nome("Voluntário de teste")
                .email("voluntario@example.com")
                .senha("hash")
                .tipo(TipoUsuario.VOLUNTARIO)
                .build();
    }
}
