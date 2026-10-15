package com.conectebem.backend.service;

import com.conectebem.backend.dto.LoginRequestDTO;
import com.conectebem.backend.dto.RegistroRequestDTO;
import com.conectebem.backend.dto.TokenResponseDTO;
import com.conectebem.backend.dto.UsuarioResponseDTO;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.UsuarioRepository;
import com.conectebem.backend.security.GoogleTokenService;
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

    @Autowired
    private GoogleTokenService googleTokenService;

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

    // Login com Google
    public TokenResponseDTO loginComGoogle(GoogleLoginRequestDTO dto) {
        // 1. Valida o token e extrai os dados via GoogleTokenService
        GoogleIdToken.Payload payload = googleTokenService.verificar(dto.idToken());
        String email = payload.getEmail();
        String nome = (String) payload.get("name");

        // 2. Busca o usuário ou cadastra caso seja o primeiro acesso
        Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario novo = new Usuario();
            novo.setNome(nome != null ? nome : "Usuário Google");
            novo.setEmail(email);
            novo.setTipo(TipoUsuario.VOLUNTARIO);
            novo.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
            return usuarioRepository.save(novo);
        });

        // 3. Emite o JWT do ConecteBem
        String token = jwtService.gerarToken(usuario);
        return new TokenResponseDTO(token, "Bearer");
    } //fecha Login Google
} // fecha a classe AuthService

