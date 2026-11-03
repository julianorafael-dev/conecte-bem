package com.conectebem.backend.repository;

import com.conectebem.backend.model.Inscricao;
import com.conectebem.backend.model.StatusInscricao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Centraliza as consultas da tabela de inscrições.
 *
 * <p>Os métodos usam os IDs dos relacionamentos para evitar que a camada de
 * serviço precise escrever SQL para consultas recorrentes.</p>
 */
public interface InscricaoRepository extends JpaRepository<Inscricao, Integer> {

    /** Localiza a inscrição única de um voluntário em uma oportunidade. */
    Optional<Inscricao> findByUsuarioIdAndOportunidadeId(Integer usuarioId, Integer oportunidadeId);

    /** Lista as candidaturas de um voluntário, das mais recentes para as mais antigas. */
    List<Inscricao> findByUsuarioIdOrderByDataInscricaoDesc(Integer usuarioId);

    /** Lista os voluntários inscritos em uma oportunidade, por ordem de inscrição. */
    List<Inscricao> findByOportunidadeIdOrderByDataInscricaoDesc(Integer oportunidadeId);

    /**
     * Conta inscrições que ocupam vaga em uma oportunidade.
     *
     * <p>O serviço informa os status ativos, normalmente PENDENTE e
     * CONFIRMADA. Inscrições CANCELADAS ficam fora da contagem.</p>
     */
    long countByOportunidadeIdAndStatusIn(
            Integer oportunidadeId,
            Collection<StatusInscricao> status
    );
}
