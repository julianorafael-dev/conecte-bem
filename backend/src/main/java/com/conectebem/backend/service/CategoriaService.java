package com.conectebem.backend.service;

import com.conectebem.backend.dto.CategoriaRequest;
import com.conectebem.backend.dto.CategoriaResponse;
import com.conectebem.backend.exception.CategoriaEmUsoException;
import com.conectebem.backend.exception.ResourceNotFoundException;
import com.conectebem.backend.model.Categoria;
import com.conectebem.backend.repository.CategoriaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;


    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listarTodas(){
        return  categoriaRepository.findAll()
                .stream()
                .map(this::paraDto)
                .toList();
    }

    public Optional<CategoriaResponse> buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .map(this::paraDto);

    }

    private CategoriaResponse paraDto(Categoria categoria){
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao()
        );
    }
    public CategoriaResponse cadastrar(CategoriaRequest categoriaRequest){
        if(categoriaRepository.existsByNomeIgnoreCase(categoriaRequest.nome())){
            throw new IllegalArgumentException("Já existe uma categoria com esse nome");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(categoriaRequest.nome());
        categoria.setDescricao(categoriaRequest.descricao());

        return paraDto(categoriaRepository.save(categoria));

    }
    public Optional<CategoriaResponse> atualizar(Long id, CategoriaRequest categoriaRequest) {
        return categoriaRepository.findById(id)
                .map(categoria -> {
                    boolean mudouNome = !categoria.getNome().equalsIgnoreCase(categoriaRequest.nome());
                    if (mudouNome && categoriaRepository.existsByNomeIgnoreCase(categoriaRequest.nome())) {
                        throw new IllegalArgumentException("Já existe uma categoria com esse nome");
                    }
                    categoria.setNome(categoriaRequest.nome());
                    categoria.setDescricao(categoriaRequest.descricao());
                    return paraDto(categoriaRepository.save(categoria));
                });
    }

    public void deletar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + id));

        try {
            categoriaRepository.delete(categoria);
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new CategoriaEmUsoException(id);
        }
    }

}
