package com.conectebem.backend.repository;

import com.conectebem.backend.model.Ong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Centraliza o acesso à tabela de ONGs.
 *
 * <p>O Spring Data fornece operações como buscar, listar e salvar sem que seja
 * necessário escrever SQL para essas ações básicas.</p>
 */
public interface OngRepository extends JpaRepository<Ong, Integer> {
    java.util.Optional<Ong> findByUsuarioId(Integer usuarioId);

    @Query("SELECT COUNT(o) > 0 FROM Ong o WHERE replace(replace(replace(o.cnpj, '.', ''), '/', ''), '-', '') = :cnpj")
    boolean existsByCnpj(@Param("cnpj") String cnpj);

    @Query("SELECT COUNT(o) > 0 FROM Ong o WHERE replace(replace(replace(o.cnpj, '.', ''), '/', ''), '-', '') = :cnpj AND o.id <> :id")
    boolean existsByCnpjAndIdNot(@Param("cnpj") String cnpj, @Param("id") Integer id);
}
