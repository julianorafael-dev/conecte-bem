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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    /** Lista todas as oportunidades cadastradas e converte para DTO. */
    // QA-015: listagem paginada (o contador de inscritos continua sendo feito em lote)
    public Page<OportunidadeResponseDTO> listar(Pageable pageable) {
        return paraPagina(oportunidadeRepository.findAll(pageable), pageable);
    }

    private Page<OportunidadeResponseDTO> paraPagina(Page<Oportunidade> pagina, Pageable pageable) {
        return new PageImpl<>(mapper.paraResponseLista(pagina.getContent()), pageable, pagina.getTotalElements());
    }

    /** Busca uma oportunidade pelo ID ou lança ResourceNotFoundException. */
    public OportunidadeResponseDTO buscarPorId(Integer id) {
        Oportunidade oportunidade = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada. ID: " + id));
        return paraResponse(oportunidade);
    }

    /**
     * Cria uma nova oportunidade vinculada à ONG do usuário autenticado.
     *
     * <p>O {@code ongId} recebido no corpo da requisição NÃO é usado para
     * definir o proprietário: a ONG é resolvida no servidor pelo vínculo
     * {@code usuarioId}. O campo permanece no DTO apenas por compatibilidade
     * com o formulário do frontend.</p>
     */
    public OportunidadeResponseDTO criar(OportunidadeRequestDTO dados, Usuario usuario) {
        validarStatus(dados.status());

        // 1. Resolver a ONG pelo usuário autenticado (não pelo ongId do corpo)
        Ong ong = ongDoUsuarioAutenticado(usuario);

        // 2. Buscar Categoria pelo categoriaId
        Integer categoriaId = dados.categoriaId();
        Categoria categoria = categoriaRepository.findById(Integer.valueOf(categoriaId))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada. ID: " + categoriaId));

        // 3. Criar nova entidade Oportunidade
        Oportunidade oportunidade = new Oportunidade();

        // Preencher relacionamentos
        oportunidade.setOng(ong);
        oportunidade.setCategoria(categoria);

        // Preencher campos do RequestDTO
        oportunidade.setTitulo(dados.titulo());
        oportunidade.setDescricao(dados.descricao());
        oportunidade.setData(dados.data());
        oportunidade.setHorario(dados.horario());
        oportunidade.setCidade(dados.cidade());
        oportunidade.setEstado(dados.estado());
        oportunidade.setVagas(dados.vagas());
        oportunidade.setStatus(dados.status());

        // 4. Registrar o momento da criação
        oportunidade.setCriadoEm(LocalDateTime.now());

        // 5. Salvar no repositório
        oportunidade = oportunidadeRepository.save(oportunidade);

        // 6. Retornar DTO de resposta
        return paraResponse(oportunidade);
    }

    /**
     * Atualiza uma oportunidade existente, desde que pertença à ONG do
     * usuário autenticado. A ONG proprietária nunca é alterada: a edição
     * não permite transferir a oportunidade para outra ONG.
     */
    @Transactional
    public OportunidadeResponseDTO atualizar(Integer id, OportunidadeRequestDTO dados, Usuario usuario) {
        validarStatus(dados.status());

        // 1. Verificar se a oportunidade existe. A linha fica bloqueada até o fim da
        // transação (mesmo bloqueio usado na inscrição), para que uma nova inscrição
        // não ocupe vaga enquanto a capacidade está sendo conferida.
        Oportunidade oportunidadeExistente = oportunidadeRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada. ID: " + id));

        // 2. Validar propriedade: oportunidade passou a pertencer à ONG do usuário autenticado
        Ong ongDoUsuario = ongDoUsuarioAutenticado(usuario);
        if (!oportunidadeExistente.getOng().getId().equals(ongDoUsuario.getId())) {
            throw new AccessDeniedException("Você só pode alterar oportunidades da sua própria ONG.");
        }

        // 3. Buscar Categoria (validar existência)
        Integer categoriaId = dados.categoriaId();
        Categoria categoria = categoriaRepository.findById(Integer.valueOf(categoriaId))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada. ID: " + categoriaId));

        // 3.1 A nova capacidade não pode ficar abaixo das inscrições ativas.
        validarCapacidadeMinima(oportunidadeExistente.getId(), dados.vagas());

        // 4. Atualizar campos permitidos
        // NÃO alterar o ID
        // NÃO alterar criadoEm
        // NÃO alterar a ONG proprietária (acompanhamento: edição não transfere a oportunidade)

        oportunidadeExistente.setCategoria(categoria);
        oportunidadeExistente.setTitulo(dados.titulo());
        oportunidadeExistente.setDescricao(dados.descricao());
        oportunidadeExistente.setData(dados.data());
        oportunidadeExistente.setHorario(dados.horario());
        oportunidadeExistente.setCidade(dados.cidade());
        oportunidadeExistente.setEstado(dados.estado());
        oportunidadeExistente.setVagas(dados.vagas());
        oportunidadeExistente.setStatus(dados.status());

        // 4. Salvar e retornar
        oportunidadeExistente = oportunidadeRepository.save(oportunidadeExistente);
        return paraResponse(oportunidadeExistente);
    }

    /**
     * Exclui uma oportunidade da ONG do usuário autenticado.
     *
     * <p>A exclusão continua física (comportamento atual). Verificação de
     * inscrições ativas e o tratamento delas dependem de acordo com a
     * Pessoa 4 e NÃO foram alterados aqui.</p>
     */
    public void excluir(Integer id, Usuario usuario) {
        // Verificar se existe antes de excluir (404 quando inexistente)
        Oportunidade oportunidade = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada. ID: " + id));

        // Validar propriedade: apenas a ONG dona pode excluir (403 caso contrário)
        Ong ongDoUsuario = ongDoUsuarioAutenticado(usuario);
        if (!oportunidade.getOng().getId().equals(ongDoUsuario.getId())) {
            throw new AccessDeniedException("Você só pode excluir oportunidades da sua própria ONG.");
        }

        oportunidadeRepository.deleteById(id);
    }

    /** Impede reduzir as vagas para menos do que o número de inscrições ativas. */
    private void validarCapacidadeMinima(Integer oportunidadeId, Integer novasVagas) {
        long inscricoesAtivas = inscricaoRepository.countByOportunidadeIdAndStatusIn(
                oportunidadeId, OportunidadeResponseMapper.STATUS_QUE_OCUPAM_VAGA);

        if (novasVagas < inscricoesAtivas) {
            throw new InscricaoConflictException(
                    "Não é possível reduzir as vagas para " + novasVagas
                            + ": já existem " + inscricoesAtivas + " inscrições ativas.");
        }
    }

    /**
     * Resolve a ONG vinculada ao usuário autenticado.
     *
     * <p>Autorização em duas camadas: contas que não são do tipo ONG recebem
     * 403; contas do tipo ONG sem registro de ONG vinculado recebem 404
     * (mesmo padrão já usado em {@code OngService.buscarPorUsuario}).</p>
     */
    private Ong ongDoUsuarioAutenticado(Usuario usuario) {
        if (usuario.getTipo() != TipoUsuario.ONG) {
            throw new AccessDeniedException("Apenas contas do tipo ONG podem gerenciar oportunidades.");
        }
        return ongRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ONG do usuário autenticado não encontrada."));
    }

    /** Filtra oportunidades por cidade. */
    public Page<OportunidadeResponseDTO> filtrarPorCidade(String cidade, Pageable pageable) {
        return paraPagina(oportunidadeRepository.findByCidade(cidade, pageable), pageable);
    }

    /** Filtra oportunidades pela categoria relacionada. */
    public Page<OportunidadeResponseDTO> filtrarPorCategoria(Integer categoriaId, Pageable pageable) {
        Categoria categoria = categoriaRepository.findById(Integer.valueOf(categoriaId))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada. ID: " + categoriaId));
        return paraPagina(oportunidadeRepository.findByCategoria(categoria, pageable), pageable);
    }

    /** Filtra oportunidades por status. */
    public Page<OportunidadeResponseDTO> filtrarPorStatus(String status, Pageable pageable) {
        validarStatus(status);
        return paraPagina(oportunidadeRepository.findByStatus(status, pageable), pageable);
    }

    /** Garante que o status seja compatível com a constraint do banco. */
    private void validarStatus(String status) {
        if (!STATUS_VALIDOS.contains(status)) {
            throw new IllegalArgumentException(
                    "O status deve ser ABERTA, ENCERRADA ou CANCELADA."
            );
        }
    }

    /** Converte entidade Oportunidade para DTO de resposta (com inscritos e vagas disponíveis). */
    private OportunidadeResponseDTO paraResponse(Oportunidade oportunidade) {
        return mapper.paraResponse(oportunidade);
    }
}
