package com.conectebem.backend.service;

import com.conectebem.backend.dto.OportunidadeResponseDTO;
import com.conectebem.backend.model.Oportunidade;
import com.conectebem.backend.model.StatusInscricao;
import com.conectebem.backend.repository.InscricaoRepository;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converte oportunidades em {@link OportunidadeResponseDTO}, calculando no
 * servidor quantos voluntários já ocupam vaga e quantas vagas restam.
 *
 * <p>Para listas, as inscrições são contadas em uma única consulta agrupada,
 * evitando uma consulta por oportunidade.</p>
 */
@Component
public class OportunidadeResponseMapper {

    /** Inscrições pendentes e confirmadas ocupam vaga; as canceladas não. */
    public static final Set<StatusInscricao> STATUS_QUE_OCUPAM_VAGA =
            EnumSet.of(StatusInscricao.PENDENTE, StatusInscricao.CONFIRMADA);

    private final InscricaoRepository inscricaoRepository;

    public OportunidadeResponseMapper(InscricaoRepository inscricaoRepository) {
        this.inscricaoRepository = inscricaoRepository;
    }

    /** Converte uma única oportunidade. */
    public OportunidadeResponseDTO paraResponse(Oportunidade oportunidade) {
        long inscritos = inscricaoRepository.countByOportunidadeIdAndStatusIn(
                oportunidade.getId(), STATUS_QUE_OCUPAM_VAGA);
        return montar(oportunidade, inscritos);
    }

    /** Converte uma lista de oportunidades com uma única consulta de contagem. */
    public List<OportunidadeResponseDTO> paraResponseLista(List<Oportunidade> oportunidades) {
        if (oportunidades.isEmpty()) {
            return List.of();
        }

        List<Integer> ids = oportunidades.stream().map(Oportunidade::getId).toList();
        Map<Integer, Long> inscritosPorOportunidade = new HashMap<>();
        for (Object[] linha : inscricaoRepository.contarAtivasPorOportunidades(ids, STATUS_QUE_OCUPAM_VAGA)) {
            inscritosPorOportunidade.put(((Number) linha[0]).intValue(), ((Number) linha[1]).longValue());
        }

        return oportunidades.stream()
                .map(o -> montar(o, inscritosPorOportunidade.getOrDefault(o.getId(), 0L)))
                .toList();
    }

    private OportunidadeResponseDTO montar(Oportunidade oportunidade, long inscritos) {
        int ocupadas = Math.toIntExact(inscritos);
        int capacidade = oportunidade.getVagas() == null ? 0 : oportunidade.getVagas();

        return new OportunidadeResponseDTO(
                oportunidade.getId(),
                oportunidade.getOng().getId(),
                oportunidade.getOng().getNome(),
                oportunidade.getCategoria().getId(),
                oportunidade.getCategoria().getNome(),
                oportunidade.getTitulo(),
                oportunidade.getDescricao(),
                oportunidade.getData(),
                oportunidade.getHorario(),
                oportunidade.getCidade(),
                oportunidade.getEstado(),
                oportunidade.getVagas(),
                oportunidade.getStatus(),
                oportunidade.getCriadoEm(),
                ocupadas,
                Math.max(0, capacidade - ocupadas)
        );
    }
}
