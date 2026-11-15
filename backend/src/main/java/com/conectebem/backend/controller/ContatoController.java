package com.conectebem.backend.controller;

import com.conectebem.backend.dto.ContatoRequestDTO;
import com.conectebem.backend.service.ContatoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expõe a rota pública do formulário de Contato do site (sem autenticação).
 */
@RestController
public class ContatoController {

    private final ContatoService contatoService;

    public ContatoController(ContatoService contatoService) {
        this.contatoService = contatoService;
    }

    @PostMapping("/contato")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enviar(@Valid @RequestBody ContatoRequestDTO dados) {
        contatoService.enviar(dados);
    }
}
