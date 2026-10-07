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

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

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