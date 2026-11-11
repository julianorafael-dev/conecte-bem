package com.conectebem.backend.service;

import com.conectebem.backend.dto.OportunidadeRequestDTO;
import com.conectebem.backend.dto.OportunidadeResponseDTO;
import com.conectebem.backend.exception.InscricaoConflictException;
import com.conectebem.backend.exception.ResourceNotFoundException;
import com.conectebem.backend.model.Categoria;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.model.Oportunidade;
import com.conectebem.backend.model.TipoUsuario;
import com.conectebem.backend.model.Usuario;
import com.conectebem.backend.repository.CategoriaRepository;
import com.conectebem.backend.repository.InscricaoRepository;
import com.conectebem.backend.repository.OngRepository;
import com.conectebem.backend.repository.OportunidadeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class OportunidadeService {

    private static final Set<String> STATUS_VALIDOS = Set.of(
            "ABERTA", "ENCERRADA", "CANCELADA"
    );

    private final OportunidadeRepository oportunidadeRepository;
    private final CategoriaRepository categoriaRepository;
    private final OngRepository ongRepository;
    private final InscricaoRepository inscricaoRepository;
    private final OportunidadeResponseMapper mapper;

    public OportunidadeService(OportunidadeRepository oportunidadeRepository,
                               CategoriaRepository categoriaRepository,
                               OngRepository ongRepository,
                               InscricaoRepository inscricaoRepository,
                               OportunidadeResponseMapper mapper) {
        this.oportunidadeRepository = oportunidadeRepository;
        this.categoriaRepository = categoriaRepository;
        this.ongRepository = ongRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.mapper = mapper;
    }

    // Correção QA-015: Agora retorna Page em vez de List
    public Page<OportunidadeResponseDTO> listar(Pageable pageable) {
        return oportunidadeRepository.findAll(pageable).map(this::paraResponse);
    }

    public OportunidadeResponseDTO buscarPorId(Integer id) {
        Oportunidade oportunidade = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada. ID: " + id));
        return paraResponse(oportunidade);
    }

    public OportunidadeResponseDTO criar(OportunidadeRequestDTO dados, Usuario usuario) {
        validarStatus(dados.status());

        Ong ong = ongDoUsuarioAutenticado(usuario);
        Integer categoriaId = dados.categoriaId();
        Categoria categoria = categoriaRepository.findById(Integer.valueOf(categoriaId))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada. ID: " + categoriaId));

        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setOng(ong);
        oportunidade.setCategoria(categoria);
        oportunidade.setTitulo(dados.titulo());
        oportunidade.setDescricao(dados.descricao());
        oportunidade.setData(dados.data());
        oportunidade.setHorario(dados.horario());
        oportunidade.setCidade(dados.cidade());
        oportunidade.setEstado(dados.estado());
        oportunidade.setVagas(dados.vagas());
        oportunidade.setStatus(dados.status());
        oportunidade.setCriadoEm(LocalDateTime.now());

        oportunidade = oportunidadeRepository.save(oportunidade);
        return paraResponse(oportunidade);
    }

    @Transactional
    public OportunidadeResponseDTO atualizar(Integer id, OportunidadeRequestDTO dados, Usuario usuario) {
        validarStatus(dados.status());

        Oportunidade oportunidadeExistente = oportunidadeRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada. ID: " + id));

        Ong ongDoUsuario = ongDoUsuarioAutenticado(usuario);
        if (!oportunidadeExistente.getOng().getId().equals(ongDoUsuario.getId())) {
            throw new AccessDeniedException("Você só pode alterar oportunidades da sua própria ONG.");
        }

        Integer categoriaId = dados.categoriaId();
        Categoria categoria = categoriaRepository.findById(Integer.valueOf(categoriaId))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada. ID: " + categoriaId));

        validarCapacidadeMinima(oportunidadeExistente.getId(), dados.vagas());

        oportunidadeExistente.setCategoria(categoria);
        oportunidadeExistente.setTitulo(dados.titulo());
        oportunidadeExistente.setDescricao(dados.descricao());
        oportunidadeExistente.setData(dados.data());
        oportunidadeExistente.setHorario(dados.horario());
        oportunidadeExistente.setCidade(dados.cidade());
        oportunidadeExistente.setEstado(dados.estado());
        oportunidadeExistente.setVagas(dados.vagas());
        oportunidadeExistente.setStatus(dados.status());

        oportunidadeExistente = oportunidadeRepository.save(oportunidadeExistente);
        return paraResponse(oportunidadeExistente);
    }

    public void excluir(Integer id, Usuario usuario) {
        Oportunidade oportunidade = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada. ID: " + id));

        Ong ongDoUsuario = ongDoUsuarioAutenticado(usuario);
        if (!oportunidade.getOng().getId().equals(ongDoUsuario.getId())) {
            throw new AccessDeniedException("Você só pode excluir oportunidades da sua própria ONG.");
        }

        oportunidadeRepository.deleteById(id);
    }

    private void validarCapacidadeMinima(Integer oportunidadeId, Integer novasVagas) {
        long inscricoesAtivas = inscricaoRepository.countByOportunidadeIdAndStatusIn(
                oportunidadeId, OportunidadeResponseMapper.STATUS_QUE_OCUPAM_VAGA);

        if (novasVagas < inscricoesAtivas) {
            throw new InscricaoConflictException(
                    "Não é possível reduzir as vagas para " + novasVagas
                            + ": já existem " + inscricoesAtivas + " inscrições ativas.");
        }
    }

    private Ong ongDoUsuarioAutenticado(Usuario usuario) {
        if (usuario.getTipo() != TipoUsuario.ONG) {
            throw new AccessDeniedException("Apenas contas do tipo ONG podem gerenciar oportunidades.");
        }
        return ongRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ONG do usuário autenticado não encontrada."));
    }

    // Correção QA-015: Passando Pageable para o repositório
    public Page<OportunidadeResponseDTO> filtrarPorCidade(String cidade, Pageable pageable) {
        return oportunidadeRepository.findByCidade(cidade, pageable).map(this::paraResponse);
    }

    // Correção QA-015
    public Page<OportunidadeResponseDTO> filtrarPorCategoria(Integer categoriaId, Pageable pageable) {
        Categoria categoria = categoriaRepository.findById(Integer.valueOf(categoriaId))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada. ID: " + categoriaId));
        return oportunidadeRepository.findByCategoria(categoria, pageable).map(this::paraResponse);
    }

    // Correção QA-015
    public Page<OportunidadeResponseDTO> filtrarPorStatus(String status, Pageable pageable) {
        validarStatus(status);
        return oportunidadeRepository.findByStatus(status, pageable).map(this::paraResponse);
    }

    private void validarStatus(String status) {
        if (!STATUS_VALIDOS.contains(status)) {
            throw new IllegalArgumentException(
                    "O status deve ser ABERTA, ENCERRADA ou CANCELADA."
            );
        }
    }

    private OportunidadeResponseDTO paraResponse(Oportunidade oportunidade) {
        return mapper.paraResponse(oportunidade);
    }
}