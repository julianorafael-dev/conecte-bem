package com.conectebem.backend.controller;


import com.conectebem.backend.dto.CategoriaRequest;
import com.conectebem.backend.dto.CategoriaResponse;
import com.conectebem.backend.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService){
        this.categoriaService = categoriaService;
    }

//   Lista todas as Categorias
    @GetMapping
    public List<CategoriaResponse> listar(){
        return categoriaService.listarTodas();
    }

//  Busca a Categoria pelo id
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> buscarPorId(@PathVariable Integer id){
        return categoriaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


//  Cria uma Categoria
    @PostMapping
    public ResponseEntity<CategoriaResponse> cadastrar(@RequestBody @Valid CategoriaRequest categoriaRequest){
        CategoriaResponse criada = categoriaService.cadastrar(categoriaRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> atualizar(@PathVariable Integer id, @Valid @RequestBody CategoriaRequest categoriaRequest){
        return categoriaService.atualizar(id, categoriaRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
