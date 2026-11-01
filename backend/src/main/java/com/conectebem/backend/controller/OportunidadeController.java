package com.conectebem.backend.controller;

import com.conectebem.backend.dto.OportunidadeRequestDTO;
import com.conectebem.backend.dto.OportunidadeResponseDTO;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.service.OportunidadeService;
import jakarta.validation.Valid;
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

    /**
     * Lista oportunidades, com filtro opcional por cidade, categoria ou status
     * (um filtro por vez — era aqui e no método listar() antigo que tínhamos
     * duas rotas GET /oportunidades, causando "Ambiguous mapping". Esse método
     * agora é o único handler de GET /oportunidades; sem nenhum parâmetro, ele
     * já cai no "else" e devolve tudo, substituindo o antigo listar()).
     */
    @GetMapping
    public ResponseEntity<List<OportunidadeResponseDTO>> filtrar(
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "categoria", required = false) Integer categoria,
            @RequestParam(value = "status", required = false) String status
    ) {
        List<OportunidadeResponseDTO> lista;

        if (cidade != null && categoria == null && status == null) {
            lista = oportunidadeService.filtrarPorCidade(cidade);
        } else if (cidade == null && categoria != null && status == null) {
            lista = oportunidadeService.filtrarPorCategoria(categoria);
        } else if (cidade == null && categoria == null && status != null) {
            lista = oportunidadeService.filtrarPorStatus(status);
        } else {
            // Nenhum filtro, ou mais de um filtro ao mesmo tempo (combinação
            // de filtros ainda não suportada nesta sprint): devolve tudo.
            lista = oportunidadeService.listar();
        }

        return ResponseEntity.ok(lista);
    }
}
