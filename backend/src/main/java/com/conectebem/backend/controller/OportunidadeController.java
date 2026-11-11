package com.conectebem.backend.controller;

import com.conectebem.backend.dto.OportunidadeRequestDTO;
import com.conectebem.backend.dto.OportunidadeResponseDTO;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.service.OportunidadeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oportunidades")
public class OportunidadeController {

    private final OportunidadeService oportunidadeService;

    public OportunidadeController(OportunidadeService oportunidadeService) {
        this.oportunidadeService = oportunidadeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OportunidadeResponseDTO> buscarPorId(@PathVariable Integer id) {
        OportunidadeResponseDTO dto = oportunidadeService.buscarPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OportunidadeResponseDTO criar(
            @Valid @RequestBody OportunidadeRequestDTO dados,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return oportunidadeService.criar(dados, usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OportunidadeResponseDTO> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody OportunidadeRequestDTO dados,
            @AuthenticationPrincipal Usuario usuario
    ) {
        OportunidadeResponseDTO dto = oportunidadeService.atualizar(id, dados, usuario);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario usuario
    ) {
        oportunidadeService.excluir(id, usuario);
    }

    /**
     * Correção QA-015: Adicionado suporte à paginação (Pageable) para evitar
     * que consultas descarreguem todos os dados do banco simultaneamente.
     */
    @GetMapping
    public ResponseEntity<Page<OportunidadeResponseDTO>> filtrar(
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "categoria", required = false) Integer categoria,
            @RequestParam(value = "status", required = false) String status,
            @PageableDefault(size = 12) Pageable pageable
    ) {
        Page<OportunidadeResponseDTO> pagina;

        if (cidade != null && categoria == null && status == null) {
            pagina = oportunidadeService.filtrarPorCidade(cidade, pageable);
        } else if (cidade == null && categoria != null && status == null) {
            pagina = oportunidadeService.filtrarPorCategoria(categoria, pageable);
        } else if (cidade == null && categoria == null && status != null) {
            pagina = oportunidadeService.filtrarPorStatus(status, pageable);
        } else {
            pagina = oportunidadeService.listar(pageable);
        }

        return ResponseEntity.ok(pagina);
    }
}