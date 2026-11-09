package com.conectebem.backend.controller;

import com.conectebem.backend.dto.InscricaoRequestDTO;
import com.conectebem.backend.dto.InscricaoResponseDTO;
import com.conectebem.backend.dto.InscritoResponseDTO;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.service.InscricaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expõe as rotas de candidatura de voluntários e consulta de inscritos por ONGs.
 */
@RestController
public class InscricaoController {

    private final InscricaoService inscricaoService;

    public InscricaoController(InscricaoService inscricaoService) {
        this.inscricaoService = inscricaoService;
    }

    /** Cria uma inscrição para o voluntário autenticado. */
    @PostMapping("/inscricoes")
    @ResponseStatus(HttpStatus.CREATED)
    public InscricaoResponseDTO inscrever(
            @Valid @RequestBody InscricaoRequestDTO dados,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return inscricaoService.inscrever(dados, usuario);
    }

    /** Lista todas as candidaturas do voluntário autenticado. */
    @GetMapping("/inscricoes/me")
    public ResponseEntity<List<InscricaoResponseDTO>> listarMinhas(
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(inscricaoService.listarMinhas(usuario));
    }

    /** Cancela a inscrição pertencente ao voluntário autenticado. */
    @DeleteMapping("/inscricoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario usuario
    ) {
        inscricaoService.cancelar(id, usuario);
    }

    /** A ONG proprietária da oportunidade confirma a inscrição. */
    @PutMapping("/inscricoes/{id}/confirmar")
    public InscritoResponseDTO confirmarPelaOng(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return inscricaoService.confirmarPelaOng(id, usuario);
    }

    /** A ONG proprietária da oportunidade cancela a inscrição. */
    @PutMapping("/inscricoes/{id}/recusar")
    public InscritoResponseDTO cancelarPelaOng(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return inscricaoService.cancelarPelaOng(id, usuario);
    }

    /** Lista os inscritos de uma oportunidade para a ONG proprietária. */
    @GetMapping("/oportunidades/{oportunidadeId}/inscricoes")
    public ResponseEntity<List<InscritoResponseDTO>> listarInscritos(
            @PathVariable Integer oportunidadeId,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(inscricaoService.listarInscritosDaOportunidade(oportunidadeId, usuario));
    }
}
