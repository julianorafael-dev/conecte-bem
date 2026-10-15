package com.conectebem.backend.service;

import com.conectebem.backend.dto.LoginRequestDTO;
import com.conectebem.backend.dto.RegistroRequestDTO;
import com.conectebem.backend.dto.TokenResponseDTO;
import com.conectebem.backend.dto.UsuarioResponseDTO;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.UsuarioRepository;
import com.conectebem.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


//Daqui para baixo refere-se ao loginGoogle

import com.conectebem.backend.dto.GoogleLoginRequestDTO;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.util.UUID;


@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // Autenticação via cliente google
    @org.springframework.beans.factory.annotation.Value("${google.client.id}")
    private String googleClientId;

    @Transactional
    public UsuarioResponseDTO registrar(RegistroRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema");
        }

        // Criptografa a senha com BCrypt antes de persistir no banco
        String senhaCriptografada = passwordEncoder.encode(dto.getSenha());

        Usuario novoUsuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(senhaCriptografada)
                .tipo(dto.getTipo())
                .build();

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        return UsuarioResponseDTO.fromEntity(usuarioSalvo);
    }

    public TokenResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        // Compara a senha informada com o hash BCrypt salvo no banco
        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        String token = jwtService.gerarToken(usuario);
        return new TokenResponseDTO(token);
    }
}

public TokenResponseDTO loginComGoogle(GoogleLoginRequestDTO dto) {
    try {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(dto.idToken());
        if (idToken == null) {
            throw new IllegalArgumentException("Token do Google inválido ou expirado.");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String nome = (String) payload.get("name");

        Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario novo = new Usuario();
            novo.setNome(nome != null ? nome : "Usuário Google");
            novo.setEmail(email);
            novo.setTipo(TipoUsuario.VOLUNTARIO);
            novo.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
            return usuarioRepository.save(novo);
        });

        String token = jwtService.gerarToken(usuario);
        return new TokenResponseDTO(token, "Bearer");

    } catch (IllegalArgumentException e) {
        throw e;
    } catch (Exception e) {
        throw new IllegalArgumentException("Falha ao validar credencial do Google: " + e.getMessage());
    }
}