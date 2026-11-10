package com.conectebem.backend.service;

import com.conectebem.backend.dto.VoluntarioPerfilResponseDTO;
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

    public VoluntarioPerfilResponseDTO buscarPerfil(Integer usuarioId) {
        return VoluntarioPerfilResponseDTO.fromEntity(buscarUsuario(usuarioId));
    }

    @Transactional
    public VoluntarioPerfilResponseDTO atualizarPerfil(Integer usuarioId, VoluntarioPerfilRequestDTO dados) {
        Usuario usuario = buscarUsuario(usuarioId);
        usuario.setNome(dados.nome().trim());
        return VoluntarioPerfilResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    private Usuario buscarUsuario(Integer usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Voluntário não encontrado."));
    }
}
