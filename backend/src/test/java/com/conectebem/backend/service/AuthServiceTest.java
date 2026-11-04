package com.conectebem.backend.service;

import com.conectebem.backend.dto.LoginRequestDTO;
import com.conectebem.backend.dto.RegistroRequestDTO;
import com.conectebem.backend.exception.CredenciaisInvalidasException;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.OngRepository;
import com.conectebem.backend.repository.UsuarioRepository;
import com.conectebem.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    // CNPJ válido de exemplo (11.222.333/0001-81)
    private static final String CNPJ_VALIDO = "11222333000181";

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final OngRepository ongRepository = mock(OngRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthService service = new AuthService();

    @BeforeEach
    void preparar() {
        ReflectionTestUtils.setField(service, "usuarioRepository", usuarioRepository);
        ReflectionTestUtils.setField(service, "ongRepository", ongRepository);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(service, "jwtService", jwtService);

        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(7);
            return u;
        });
    }

    private RegistroRequestDTO registro(TipoUsuario tipo, String cnpj) {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setNome("  ONG Esperança  ");
        dto.setEmail("ong@example.com");
        dto.setSenha("123456");
        dto.setTipo(tipo);
        dto.setCnpj(cnpj);
        return dto;
    }

    @Test
    void registraOngCriandoUsuarioEOngVinculados() {
        RegistroRequestDTO dto = registro(TipoUsuario.ONG, "11.222.333/0001-81");
        dto.setTelefone("71999990000");
        dto.setEstado("ba");
        dto.setCidade("   ");

        var resposta = service.registrar(dto);

        assertEquals(7, resposta.getId());
        assertEquals("ONG Esperança", resposta.getNome());

        ArgumentCaptor<Ong> captor = ArgumentCaptor.forClass(Ong.class);
        verify(ongRepository).save(captor.capture());
        Ong ong = captor.getValue();
        assertEquals(7, ong.getUsuarioId());
        assertEquals("ONG Esperança", ong.getNome());
        assertEquals(CNPJ_VALIDO, ong.getCnpj());
        assertEquals("71999990000", ong.getTelefone());
        assertEquals("BA", ong.getEstado());
        assertNull(ong.getCidade());
    }

    @Test
    void registraVoluntarioSemCriarOng() {
        service.registrar(registro(TipoUsuario.VOLUNTARIO, null));

        verify(usuarioRepository).save(any(Usuario.class));
        verify(ongRepository, never()).save(any(Ong.class));
    }

    @Test
    void rejeitaOngSemCnpjSemGravarNada() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(registro(TipoUsuario.ONG, null)));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(ongRepository, never()).save(any(Ong.class));
    }

    @Test
    void rejeitaCnpjComDigitosVerificadoresErrados() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(registro(TipoUsuario.ONG, "11222333000182")));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void rejeitaCnpjJaCadastrado() {
        when(ongRepository.existsByCnpj(CNPJ_VALIDO)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(registro(TipoUsuario.ONG, CNPJ_VALIDO)));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void rejeitaEstadoInvalido() {
        RegistroRequestDTO dto = registro(TipoUsuario.ONG, CNPJ_VALIDO);
        dto.setEstado("Bahia");

        assertThrows(IllegalArgumentException.class, () -> service.registrar(dto));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void rejeitaEmailJaCadastrado() {
        when(usuarioRepository.existsByEmail("ong@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(registro(TipoUsuario.ONG, CNPJ_VALIDO)));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(ongRepository, never()).save(any(Ong.class));
    }

    @Test
    void loginComSenhaIncorretaLancaCredenciaisInvalidas() {
        Usuario usuario = Usuario.builder().id(7).email("a@a.com").senha("hash")
                .tipo(TipoUsuario.VOLUNTARIO).build();
        when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("a@a.com");
        dto.setSenha("errada");

        assertThrows(CredenciaisInvalidasException.class, () -> service.login(dto));
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    void loginComEmailInexistenteLancaCredenciaisInvalidas() {
        when(usuarioRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("x@x.com");
        dto.setSenha("qualquer");

        assertThrows(CredenciaisInvalidasException.class, () -> service.login(dto));
    }

    @Test
    void loginComCredenciaisCorretasRetornaToken() {
        Usuario usuario = Usuario.builder().id(7).email("a@a.com").senha("hash")
                .tipo(TipoUsuario.ONG).build();
        when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("certa", "hash")).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn("jwt-abc");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("a@a.com");
        dto.setSenha("certa");

        assertEquals("jwt-abc", service.login(dto).getToken());
    }
}
