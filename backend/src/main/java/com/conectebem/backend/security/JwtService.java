package com.conectebem.backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.conectebem.backend.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    private static final Duration VALIDADE_TOKEN = Duration.ofHours(2);

    private final String secret;
    private final Clock clock;

    /**
     * O segredo é obrigatório e vem exclusivamente de configuração externa.
     * Não há valor padrão para evitar que uma chave conhecida seja usada em produção.
     */
    @Autowired
    public JwtService(@Value("${api.security.token.secret}") String secret) {
        this(secret, Clock.systemUTC());
    }

    /** Construtor adicional para testes determinísticos de expiração. */
    JwtService(String secret, Clock clock) {
        if (secret == null || secret.isBlank() || secret.trim().length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET deve ser informado e ter pelo menos 32 caracteres."
            );
        }
        this.secret = secret;
        this.clock = clock;
    }

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("conecte-bem-api")
                    .withSubject(usuario.getEmail())
                    .withClaim("id", usuario.getId())
                    .withClaim("tipo", usuario.getTipo().name())
                    .withExpiresAt(gerarDataExpiracao())
                    .sign(algoritmo);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validarToken(String token) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            JWTVerifier.BaseVerification verificacao = (JWTVerifier.BaseVerification) JWT.require(algoritmo)
                    .withIssuer("conecte-bem-api");
            return verificacao.build(clock)
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null; // Retorna null se o token for inválido ou expirado
        }
    }

    private Instant gerarDataExpiracao() {
        // A expiração é calculada a partir de um instante UTC; por isso não
        // depende do fuso horário da máquina onde a API estiver rodando.
        return clock.instant().plus(VALIDADE_TOKEN);
    }
}
