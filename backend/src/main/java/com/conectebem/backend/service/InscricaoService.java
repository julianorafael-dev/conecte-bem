package com.conectebem.backend.service;

import com.conectebem.backend.dto.InscricaoRequestDTO;
import com.conectebem.backend.dto.InscricaoResponseDTO;
import com.conectebem.backend.dto.InscritoResponseDTO;
import com.conectebem.backend.exception.InscricaoConflictException;
import com.conectebem.backend.exception.ResourceNotFoundException;
import com.conectebem.backend.model.Inscricao;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.model.Oportunidade;
import com.conectebem.backend.model.StatusInscricao;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.InscricaoRepository;
import com.conectebem.backend.repository.OngRepository;
import com.conectebem.backend.repository.OportunidadeRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

/**
 * Centraliza as regras de negócio das candidaturas a oportunidades.
 *
 * <p>As regras aplicadas são: somente voluntários se inscrevem, apenas
 * oportunidades abertas recebem candidaturas, vagas ativas não podem exceder
 * a capacidade e uma ONG só visualiza inscritos das próprias oportunidades.</p>
 */
@Service
public class InscricaoService {

    private static final String STATUS_OPORTUNIDADE_ABERTA = "ABERTA";
    private static final EnumSet<StatusInscricao> STATUS_QUE_OCUPAM_VAGA =
            EnumSet.of(StatusInscricao.PENDENTE, StatusInscricao.CONFIRMADA);

    private final InscricaoRepository inscricaoRepository;
    private final OportunidadeRepository oportunidadeRepository;
    private final OngRepository ongRepository;

    public InscricaoService(
            InscricaoRepository inscricaoRepository,
            OportunidadeRepository oportunidadeRepository,
            OngRepository ongRepository
    ) {
        this.inscricaoRepository = inscricaoRepository;
        this.oportunidadeRepository = oportunidadeRepository;
        this.ongRepository = ongRepository;
    }

    /**
     * Cria uma candidatura ou reativa uma candidatura anteriormente cancelada.
     *
     * <p>A oportunidade é bloqueada durante a conferência de vagas. Assim,
     * duas requisições simultâneas não conseguem ocupar a mesma última vaga.</p>
     */
    @Transactional
    public InscricaoResponseDTO inscrever(InscricaoRequestDTO dados, Usuario usuario) {
        validarVoluntario(usuario);

        Oportunidade oportunidade = oportunidadeRepository.findByIdForUpdate(dados.oportunidadeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Oportunidade não encontrada. ID: " + dados.oportunidadeId()
                ));

        validarOportunidadeAberta(oportunidade);

        Inscricao inscricaoExistente = inscricaoRepository
                .findByUsuarioIdAndOportunidadeId(usuario.getId(), oportunidade.getId())
                .orElse(null);

        if (inscricaoExistente != null && inscricaoExistente.getStatus() != StatusInscricao.CANCELADA) {
            throw new InscricaoConflictException("Você já possui uma inscrição ativa nesta oportunidade.");
        }

        validarCapacidadeDisponivel(oportunidade);

        Inscricao inscricao;
        if (inscricaoExistente != null) {
            inscricao = inscricaoExistente;
            inscricao.setStatus(StatusInscricao.PENDENTE);
            inscricao.setDataInscricao(LocalDateTime.now());
        } else {
            inscricao = new Inscricao();
            inscricao.setUsuario(usuario);
            inscricao.setOportunidade(oportunidade);
            inscricao.setStatus(StatusInscricao.PENDENTE);
            inscricao.setDataInscricao(LocalDateTime.now());
        }

        return paraResponse(inscricaoRepository.save(inscricao));
    }

    /** Lista as candidaturas do voluntário autenticado. */
    @Transactional(readOnly = true)
    public List<InscricaoResponseDTO> listarMinhas(Usuario usuario) {
        validarVoluntario(usuario);

        return inscricaoRepository.findByUsuarioIdOrderByDataInscricaoDesc(usuario.getId())
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    /** Cancela a inscrição pertencente ao voluntário autenticado. */
    @Transactional
    public void cancelar(Integer inscricaoId, Usuario usuario) {
        validarVoluntario(usuario);

        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscrição não encontrada. ID: " + inscricaoId));

        if (!inscricao.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Você só pode cancelar suas próprias inscrições.");
        }

        if (inscricao.getStatus() != StatusInscricao.CANCELADA) {
            inscricao.setStatus(StatusInscricao.CANCELADA);
            inscricaoRepository.save(inscricao);
        }
    }

    /**
     * Lista os inscritos de uma oportunidade apenas para a ONG proprietária.
     *
     * <p>Inscrições canceladas também são devolvidas, identificadas pelo
     * status, para preservar o histórico de candidaturas da oportunidade.</p>
     */
    @Transactional(readOnly = true)
    public List<InscritoResponseDTO> listarInscritosDaOportunidade(Integer oportunidadeId, Usuario usuario) {
        Ong ong = obterOngDoUsuarioAutenticado(usuario);

        Oportunidade oportunidade = oportunidadeRepository.findById(oportunidadeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Oportunidade não encontrada. ID: " + oportunidadeId
                ));

        if (!oportunidade.getOng().getId().equals(ong.getId())) {
            throw new AccessDeniedException("Você só pode consultar inscritos das oportunidades da sua ONG.");
        }

        return inscricaoRepository.findByOportunidadeIdOrderByDataInscricaoDesc(oportunidadeId)
                .stream()
                .map(this::paraInscritoResponse)
                .toList();
    }

    private void validarVoluntario(Usuario usuario) {
        if (usuario == null || usuario.getTipo() != TipoUsuario.VOLUNTARIO) {
            throw new AccessDeniedException("Apenas contas do tipo voluntário podem realizar esta ação.");
        }
    }

    private Ong obterOngDoUsuarioAutenticado(Usuario usuario) {
        if (usuario == null || usuario.getTipo() != TipoUsuario.ONG) {
            throw new AccessDeniedException("Apenas contas do tipo ONG podem realizar esta ação.");
        }

        return ongRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ONG do usuário autenticado não encontrada."));
    }

    private void validarOportunidadeAberta(Oportunidade oportunidade) {
        if (!STATUS_OPORTUNIDADE_ABERTA.equals(oportunidade.getStatus())) {
            throw new InscricaoConflictException("Esta oportunidade não está aberta para inscrições.");
        }
    }

    private void validarCapacidadeDisponivel(Oportunidade oportunidade) {
        long inscricoesAtivas = inscricaoRepository.countByOportunidadeIdAndStatusIn(
                oportunidade.getId(),
                STATUS_QUE_OCUPAM_VAGA
        );

        if (inscricoesAtivas >= oportunidade.getVagas()) {
            throw new InscricaoConflictException("Não há vagas disponíveis nesta oportunidade.");
        }
    }

    private InscricaoResponseDTO paraResponse(Inscricao inscricao) {
        Oportunidade oportunidade = inscricao.getOportunidade();

        return new InscricaoResponseDTO(
                inscricao.getId(),
                oportunidade.getId(),
                oportunidade.getTitulo(),
                oportunidade.getOng().getNome(),
                oportunidade.getData(),
                oportunidade.getHorario(),
                oportunidade.getCidade(),
                oportunidade.getEstado(),
                oportunidade.getDescricao(),
                inscricao.getStatus(),
                oportunidade.getStatus(),
                inscricao.getDataInscricao()
        );
    }

    private InscritoResponseDTO paraInscritoResponse(Inscricao inscricao) {
        Usuario voluntario = inscricao.getUsuario();

        return new InscritoResponseDTO(
                inscricao.getId(),
                voluntario.getId(),
                voluntario.getNome(),
                voluntario.getEmail(),
                inscricao.getStatus(),
                inscricao.getDataInscricao()
        );
    }
}
