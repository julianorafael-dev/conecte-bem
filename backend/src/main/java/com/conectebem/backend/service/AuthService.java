package com.conectebem.backend.service;

import com.conectebem.backend.dto.LoginRequestDTO;
import com.conectebem.backend.dto.RegistroRequestDTO;
import com.conectebem.backend.dto.TokenResponseDTO;
import com.conectebem.backend.dto.UsuarioResponseDTO;
import com.conectebem.backend.exception.CredenciaisInvalidasException;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.OngRepository;
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


@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OngRepository ongRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GoogleTokenService googleTokenService;

    // Autenticação via cliente google
    @org.springframework.beans.factory.annotation.Value("${google.client.id}")
    private String googleClientId;

    /**
     * Registra um usuário. Quando o tipo é ONG, o cadastro complementar (tabela
     * {@code ongs}) é criado na MESMA transação: se qualquer etapa falhar, nada é gravado.
     */
    @Transactional
    public UsuarioResponseDTO registrar(RegistroRequestDTO dto) {
        if (dto.getTipo() == TipoUsuario.VOLUNTARIO) {
            IdadeMinima.validar(dto.getDataNascimento(),
                    java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")));
        }
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema");
        }

        // Valida os dados da ONG antes de gravar qualquer coisa
        String cnpj = null;
        String estado = null;
        if (dto.getTipo() == TipoUsuario.ONG) {
            cnpj = CnpjValidador.normalizar(dto.getCnpj());
            if (ongRepository.existsByCnpj(cnpj)) {
                throw new IllegalArgumentException("CNPJ já cadastrado no sistema");
            }
            estado = normalizarEstado(dto.getEstado());
        }

        // Criptografa a senha com BCrypt antes de persistir no banco
        String senhaCriptografada = passwordEncoder.encode(dto.getSenha());

        Usuario novoUsuario = Usuario.builder()
                .nome(dto.getNome().trim())
                .email(dto.getEmail())
                .senha(senhaCriptografada)
                .tipo(dto.getTipo())
                .build();

        if (dto.getTipo() == TipoUsuario.VOLUNTARIO) {
            String cpf = vazioParaNulo(dto.getCpf());
            novoUsuario.setCpf(cpf == null ? null : cpf.replaceAll("[.\\-]", ""));
            novoUsuario.setDataNascimento(dto.getDataNascimento());
            novoUsuario.setTelefone(vazioParaNulo(dto.getTelefone()));
        }

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        if (dto.getTipo() == TipoUsuario.ONG) {
            Ong ong = new Ong();
            ong.setUsuarioId(usuarioSalvo.getId());
            ong.setNome(usuarioSalvo.getNome());
            ong.setCnpj(cnpj);
            ong.setDescricao(vazioParaNulo(dto.getDescricao()));
            ong.setTelefone(vazioParaNulo(dto.getTelefone()));
            ong.setDataCriacao(dto.getDataCriacao());
            ong.setCidade(vazioParaNulo(dto.getCidade()));
            ong.setEstado(estado);
            ongRepository.save(ong);
        }

        return UsuarioResponseDTO.fromEntity(usuarioSalvo);
    }

    private static String normalizarEstado(String estado) {
        String valor = vazioParaNulo(estado);
        if (valor == null) {
            return null;
        }
        valor = valor.toUpperCase();
        if (!valor.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("O estado deve conter duas letras (UF)");
        }
        return valor;
    }

    private static String vazioParaNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    public TokenResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(CredenciaisInvalidasException::new);

        // Compara a senha informada com o hash BCrypt salvo no banco
        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        String token = jwtService.gerarToken(usuario);
        return new TokenResponseDTO(token);
    }

    // Login com Google
    public TokenResponseDTO loginComGoogle(GoogleLoginRequestDTO dto) {
        // 1. Valida o token e extrai os dados via GoogleTokenService
        GoogleIdToken.Payload payload = googleTokenService.verificar(dto.idToken());
        String email = payload.getEmail();
        // 2. Busca uma conta já cadastrada
        // O token Google não informa nascimento. Evita criar conta sem verificar idade.
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new IllegalArgumentException("Crie sua conta pelo formulário de cadastro, informando sua data de nascimento, antes de entrar com Google."));

        // 3. Emite o JWT do ConecteBem
        String token = jwtService.gerarToken(usuario);
        return new TokenResponseDTO(token, "Bearer");
    } //fecha Login Google
} // fecha a classe AuthService

