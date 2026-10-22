package com.conectebem.backend.controller;

import com.conectebem.backend.dto.UsuarioResponseDTO;
import com.conectebem.backend.dto.VoluntarioPerfilRequestDTO;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.service.VoluntarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voluntarios/me")
public class VoluntarioController {
    private final VoluntarioService voluntarioService;

    public VoluntarioController(VoluntarioService voluntarioService) {
        this.voluntarioService = voluntarioService;
    }

    @GetMapping
    public UsuarioResponseDTO meuPerfil(@AuthenticationPrincipal Usuario usuario) {
        return voluntarioService.buscarPerfil(usuario.getId());
    }

    @PutMapping
    public UsuarioResponseDTO atualizarMeuPerfil(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody VoluntarioPerfilRequestDTO dados) {
        return voluntarioService.atualizarPerfil(usuario.getId(), dados);
    }
}
