package com.conectebem.backend.service;

import com.conectebem.backend.dto.UsuarioResponseDTO;
import com.conectebem.backend.dto.VoluntarioPerfilRequestDTO;
import com.conectebem.backend.exception.ResourceNotFoundException;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoluntarioService {
    private final UsuarioRepository usuarioRepository;

    public VoluntarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponseDTO buscarPerfil(Integer usuarioId) {
        return UsuarioResponseDTO.fromEntity(buscarUsuario(usuarioId));
    }

    @Transactional
    public UsuarioResponseDTO atualizarPerfil(Integer usuarioId, VoluntarioPerfilRequestDTO dados) {
        Usuario usuario = buscarUsuario(usuarioId);
        usuario.setNome(dados.nome().trim());
        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    private Usuario buscarUsuario(Integer usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Voluntário não encontrado."));
    }
}
