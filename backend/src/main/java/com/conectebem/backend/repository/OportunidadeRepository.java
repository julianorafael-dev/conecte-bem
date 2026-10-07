package com.conectebem.backend.repository;

import com.conectebem.backend.model.Categoria;
import com.conectebem.backend.model.Oportunidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Centraliza o acesso à tabela de oportunidades.
 *
 * <p>O Spring Data fornece operações como buscar, listar e salvar sem que seja
 * necessário escrever SQL para essas ações básicas. Também inclui métodos
 * de filtro para buscas por cidade, categoria e status.</p>
 */
public interface OportunidadeRepository extends JpaRepository<Oportunidade, Integer> {

    /** Busca oportunidades filtradas por cidade. */
    List<Oportunidade> findByCidade(String cidade);

    /** Busca oportunidades filtradas pela categoria relacionada. */
    List<Oportunidade> findByCategoria(Categoria categoria);

    /** Busca oportunidades filtradas por status. */
    List<Oportunidade> findByStatus(String status);
}