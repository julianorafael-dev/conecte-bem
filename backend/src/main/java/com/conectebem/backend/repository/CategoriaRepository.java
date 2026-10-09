package com.conectebem.backend.repository;

import com.conectebem.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    boolean existsByNomeIgnoreCase(String nome);
}
