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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

/**
 * Centraliza as regras de negócio das candidaturas a oportunidades.
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

    @Transactional
    public InscricaoResponseDTO inscrever(InscricaoRequestDTO dados, Usuario usuario) {
        validarVoluntario(usuario);

        Oportunidade oportunidade = oportunidadeRepository.findByIdForUpdate(dados.oportunidadeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Oportunidade não encontrada. ID: " + dados.oportunidadeId()
                ));

        validarOportunidadeAberta(oportunidade);

        // Correção QA-003: Impede inscrições em oportunidades cuja data já passou
        if (oportunidade.getData() != null && oportunidade.getData().isBefore(LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")))) {
            throw new InscricaoConflictException("Esta oportunidade já ocorreu e não aceita novas inscrições.");
        }

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

    @Transactional(readOnly = true)
    public List<InscricaoResponseDTO> listarMinhas(Usuario usuario) {
        validarVoluntario(usuario);

        return inscricaoRepository.findByUsuarioIdOrderByDataInscricaoDesc(usuario.getId())
                .stream()
                .map(this::paraResponse)
                .toList();
    }

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

    @Transactional
    public InscritoResponseDTO confirmarPelaOng(Integer inscricaoId, Usuario usuario) {
        Inscricao inscricao = obterInscricaoDaOng(inscricaoId, usuario);

        if (inscricao.getStatus() == StatusInscricao.CANCELADA) {
            throw new InscricaoConflictException("Não é possível confirmar uma inscrição cancelada.");
        }

        if (inscricao.getStatus() != StatusInscricao.CONFIRMADA) {
            inscricao.setStatus(StatusInscricao.CONFIRMADA);
            inscricaoRepository.save(inscricao);
        }
        return paraInscritoResponse(inscricao);
    }

    @Transactional
    public InscritoResponseDTO cancelarPelaOng(Integer inscricaoId, Usuario usuario) {
        Inscricao inscricao = obterInscricaoDaOng(inscricaoId, usuario);

        if (inscricao.getStatus() != StatusInscricao.CANCELADA) {
            inscricao.setStatus(StatusInscricao.CANCELADA);
            inscricaoRepository.save(inscricao);
        }
        return paraInscritoResponse(inscricao);
    }

    private Inscricao obterInscricaoDaOng(Integer inscricaoId, Usuario usuario) {
        Ong ong = obterOngDoUsuarioAutenticado(usuario);

        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscrição não encontrada. ID: " + inscricaoId));

        if (!inscricao.getOportunidade().getOng().getId().equals(ong.getId())) {
            throw new AccessDeniedException("Você só pode alterar inscrições das oportunidades da sua ONG.");
        }
        return inscricao;
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
