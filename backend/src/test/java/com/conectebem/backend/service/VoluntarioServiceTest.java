package com.conectebem.backend.service;

import com.conectebem.backend.dto.VoluntarioPerfilRequestDTO;
import com.conectebem.backend.exception.ResourceNotFoundException;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VoluntarioServiceTest {
    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final VoluntarioService service = new VoluntarioService(repository);

    @Test
    void atualizaNomePreservandoDadosDaConta() {
        Usuario usuario = Usuario.builder()
                .id(7).nome("Nome antigo").email("voluntario@example.com")
                .senha("hash-existente").tipo(TipoUsuario.VOLUNTARIO).build();
        when(repository.findById(7)).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);

        var resposta = service.atualizarPerfil(7, new VoluntarioPerfilRequestDTO("  Novo nome  "));

        assertEquals("Novo nome", resposta.getNome());
        assertEquals("voluntario@example.com", resposta.getEmail());
        assertEquals("hash-existente", usuario.getSenha());
        assertEquals(TipoUsuario.VOLUNTARIO, resposta.getTipo());
        verify(repository).findById(7);
        verify(repository).save(usuario);
    }

    @Test
    void perfilInexistenteNaoPersisteDados() {
        when(repository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.atualizarPerfil(7, new VoluntarioPerfilRequestDTO("Nome")));

        verify(repository, never()).save(any());
    }
}
