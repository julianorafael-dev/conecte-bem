package com.conectebem.backend.repository;

import com.conectebem.backend.model.Categoria;
import com.conectebem.backend.model.Oportunidade;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Centraliza o acesso à tabela de oportunidades.
 *
 * <p>O Spring Data fornece operações como buscar, listar e salvar sem que seja
 * necessário escrever SQL para essas ações básicas. Também inclui métodos
 * de filtro para buscas por cidade, categoria e status.</p>
 */
public interface OportunidadeRepository extends JpaRepository<Oportunidade, Integer> {

    /** Busca oportunidades filtradas por cidade. */
    Page<Oportunidade> findByCidade(String cidade, Pageable pageable);

    /** Busca oportunidades filtradas pela categoria relacionada. */
    Page<Oportunidade> findByCategoria(Categoria categoria, Pageable pageable);

    /** Busca oportunidades filtradas por status. */
    Page<Oportunidade> findByStatus(String status, Pageable pageable);

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

    /**
     * Lista todas as oportunidades da ONG vinculada ao usuário informado.
     *
     * <p>Navega pelo relacionamento {@code Oportunidade.ong} e pelo atributo
     * {@code Ong.usuarioId} (o id do perfil de usuário, não o id da ONG).
     * Retorna em ordem decrescente de criação.</p>
     */
    @Query("SELECT o FROM Oportunidade o WHERE o.ong.usuarioId = :usuarioId ORDER BY o.criadoEm DESC")
    List<Oportunidade> findByOngUsuarioId(@Param("usuarioId") Integer usuarioId);

    /**
     * Busca uma oportunidade bloqueando sua linha durante uma nova inscrição.
     *
     * <p>O bloqueio pessimista evita que duas requisições concorrentes contem
     * a última vaga ao mesmo tempo e ultrapassem a capacidade definida.</p>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Oportunidade o WHERE o.id = :id")
    Optional<Oportunidade> findByIdForUpdate(@Param("id") Integer id);
}
