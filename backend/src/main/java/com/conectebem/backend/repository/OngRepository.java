package com.conectebem.backend.repository;

import com.conectebem.backend.model.Ong;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Centraliza o acesso à tabela de ONGs.
 *
 * <p>O Spring Data fornece operações como buscar, listar e salvar sem que seja
 * necessário escrever SQL para essas ações básicas.</p>
 */
public interface OngRepository extends JpaRepository<Ong, Integer> {
}
