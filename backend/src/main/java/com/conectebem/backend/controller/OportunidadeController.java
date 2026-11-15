package com.conectebem.backend.controller;

import com.conectebem.backend.dto.OportunidadeRequestDTO;
import com.conectebem.backend.dto.OportunidadeResponseDTO;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.service.OportunidadeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/oportunidades")
public class OportunidadeController {

    private final OportunidadeService oportunidadeService;

    public OportunidadeController(OportunidadeService oportunidadeService) {
        this.oportunidadeService = oportunidadeService;
    }

    /** Busca uma oportunidade pelo ID. */
    @GetMapping("/{id}")
    public ResponseEntity<OportunidadeResponseDTO> buscarPorId(@PathVariable Integer id) {
        OportunidadeResponseDTO dto = oportunidadeService.buscarPorId(id);
        return ResponseEntity.ok(dto);
    }

    /** Cria uma nova oportunidade vinculada à ONG do usuário autenticado. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OportunidadeResponseDTO criar(
            @Valid @RequestBody OportunidadeRequestDTO dados,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return oportunidadeService.criar(dados, usuario);
    }

    /** Atualiza uma oportunidade existente da ONG do usuário autenticado. */
    @PutMapping("/{id}")
    public ResponseEntity<OportunidadeResponseDTO> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody OportunidadeRequestDTO dados,
            @AuthenticationPrincipal Usuario usuario
    ) {
        OportunidadeResponseDTO dto = oportunidadeService.atualizar(id, dados, usuario);
        return ResponseEntity.ok(dto);
    }

    /** Exclui uma oportunidade da ONG do usuário autenticado. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario usuario
    ) {
        oportunidadeService.excluir(id, usuario);
    }

    /** Tamanho máximo de página aceito em GET /oportunidades (QA-015). */
    private static final int TAMANHO_MAXIMO = 100;

    /**
     * Lista oportunidades, com filtro opcional por cidade, categoria ou status
     * (um filtro por vez). O corpo continua sendo uma lista JSON; a listagem é
     * limitada por página ({@code page} e {@code size}, no máximo 100) e o total
     * de registros vem no cabeçalho {@code X-Total-Count}.
     */
    @GetMapping
    public ResponseEntity<List<OportunidadeResponseDTO>> filtrar(
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "categoria", required = false) Integer categoria,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "100") int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), TAMANHO_MAXIMO),
                Sort.by("id"));
        Page<OportunidadeResponseDTO> pagina;

        if (cidade != null && categoria == null && status == null) {
            pagina = oportunidadeService.filtrarPorCidade(cidade, pageable);
        } else if (cidade == null && categoria != null && status == null) {
            pagina = oportunidadeService.filtrarPorCategoria(categoria, pageable);
        } else if (cidade == null && categoria == null && status != null) {
            pagina = oportunidadeService.filtrarPorStatus(status, pageable);
        } else {
            // Nenhum filtro, ou mais de um filtro ao mesmo tempo (combinação
            // de filtros ainda não suportada nesta sprint): lista sem filtro.
            pagina = oportunidadeService.listar(pageable);
        }

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(pagina.getTotalElements()))
                .body(pagina.getContent());
    }
}
