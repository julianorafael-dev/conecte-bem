package com.conectebem.backend.repository;

import com.conectebem.backend.model.Categoria;
import com.conectebem.backend.model.Oportunidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /** Conta todas as oportunidades pertencentes à ONG vinculada ao usuário. */
    long countByOngUsuarioId(Integer usuarioId);

    /** Conta oportunidades da ONG por status, como ABERTA ou ENCERRADA. */
    long countByOngUsuarioIdAndStatus(Integer usuarioId, String status);

    /**
     * Conta inscrições nas oportunidades da ONG.
     *
     * <p>A métrica considera apenas inscrições pendentes ou confirmadas. As
     * inscrições canceladas não representam voluntários ativos e, por isso,
     * não entram no indicador do painel.</p>
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM inscricoes inscricao
            INNER JOIN oportunidades oportunidade ON oportunidade.id = inscricao.oportunidade_id
            INNER JOIN ongs ong ON ong.id = oportunidade.ong_id
            WHERE ong.usuario_id = :usuarioId
              AND inscricao.status IN ('PENDENTE', 'CONFIRMADA')
            """, nativeQuery = true)
    long countInscricoesByOngUsuarioId(@Param("usuarioId") Integer usuarioId);
}
