package com.conectebem.backend.controller;

import com.conectebem.backend.dto.OngRequestDTO;
import com.conectebem.backend.dto.OngResponseDTO;
import com.conectebem.backend.dto.PainelOngResponseDTO;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.service.OngService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expõe as rotas HTTP do módulo ONG.
 *
 * <p>A autenticação e a autorização dessas rotas são responsabilidade da
 * configuração de segurança; este controller concentra somente o recurso ONG.</p>
 */
@RestController
@RequestMapping("/ongs")
public class OngController {

    @GetMapping("/me")
    public OngResponseDTO meuPerfil(@AuthenticationPrincipal Usuario usuario) {
        return ongService.buscarPerfil(usuario.getId());
    }

    /** Retorna os indicadores do painel da ONG autenticada. */
    @GetMapping("/me/painel")
    public PainelOngResponseDTO meuPainel(@AuthenticationPrincipal Usuario usuario) {
        return ongService.buscarResumoPainel(usuario.getId());
    }

    @PutMapping("/me")
    public OngResponseDTO atualizarMeuPerfil(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody com.conectebem.backend.dto.OngPerfilRequestDTO dados) {
        return ongService.atualizarPerfil(usuario.getId(), dados);
    }

    private final OngService ongService;

    public OngController(OngService ongService) {
        this.ongService = ongService;
    }

    /** Retorna todas as ONGs cadastradas. */
    @GetMapping
    public List<OngResponseDTO> listar() {
        return ongService.listar();
    }

    /** Retorna uma ONG pelo identificador. */
    @GetMapping("/{id}")
    public OngResponseDTO buscarPorId(@PathVariable Integer id) {
        return ongService.buscarPorId(id);
    }

    /** Cria uma ONG e retorna HTTP 201 quando o cadastro é concluído. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OngResponseDTO criar(@Valid @RequestBody OngRequestDTO dados) {
        return ongService.criar(dados);
    }

    /** Atualiza os dados de uma ONG existente. */
    @PutMapping("/{id}")
    public OngResponseDTO atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody OngRequestDTO dados
    ) {
        return ongService.atualizar(id, dados);
    }
}
