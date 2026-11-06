package com.conectebem.backend.service;

import com.conectebem.backend.dto.OngRequestDTO;
import com.conectebem.backend.dto.OngResponseDTO;
import com.conectebem.backend.dto.OportunidadeResponseDTO;
import com.conectebem.backend.dto.PainelOngResponseDTO;
import com.conectebem.backend.exception.ResourceNotFoundException;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.model.Oportunidade;
import com.conectebem.backend.repository.OngRepository;
import com.conectebem.backend.repository.OportunidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Contém as regras de negócio do módulo ONG.
 *
 * <p>Esta camada recebe dados do controller, usa o repositório para acessar o
 * banco e devolve DTOs para evitar expor a entidade diretamente pela API.</p>
 */
@Service
public class OngService {

    private static final String STATUS_ABERTA = "ABERTA";
    private static final String STATUS_ENCERRADA = "ENCERRADA";

    public OngResponseDTO buscarPerfil(Integer usuarioId) {
        return paraResponse(buscarPorUsuario(usuarioId));
    }

    public OngResponseDTO atualizarPerfil(Integer usuarioId, com.conectebem.backend.dto.OngPerfilRequestDTO dados) {
        Ong ong = buscarPorUsuario(usuarioId);
        ong.setNome(dados.nome().trim());
        ong.setDescricao(dados.descricao());
        ong.setTelefone(dados.telefone());
        ong.setCidade(dados.cidade());
        ong.setEstado(dados.estado());
        return paraResponse(ongRepository.save(ong));
    }

    private Ong buscarPorUsuario(Integer usuarioId) {
        return ongRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("ONG não encontrada."));
    }

    private final OngRepository ongRepository;
    private final OportunidadeRepository oportunidadeRepository;

    public OngService(OngRepository ongRepository, OportunidadeRepository oportunidadeRepository) {
        this.ongRepository = ongRepository;
        this.oportunidadeRepository = oportunidadeRepository;
    }

    /**
     * Retorna os indicadores da ONG vinculada ao usuário autenticado.
     *
     * <p>A busca prévia da ONG assegura que somente uma conta com perfil de
     * ONG cadastrado receba os dados do painel.</p>
     */
    @Transactional(readOnly = true)
    public PainelOngResponseDTO buscarResumoPainel(Integer usuarioId) {
        Ong ong = buscarPorUsuario(usuarioId);

        return new PainelOngResponseDTO(
                ong.getNome(),
                oportunidadeRepository.countByOngUsuarioId(usuarioId),
                oportunidadeRepository.countByOngUsuarioIdAndStatus(usuarioId, STATUS_ABERTA),
                oportunidadeRepository.countByOngUsuarioIdAndStatus(usuarioId, STATUS_ENCERRADA),
                oportunidadeRepository.countInscricoesByOngUsuarioId(usuarioId)
        );
    }

    /**
     * Lista as oportunidades da ONG vinculada ao usuário autenticado.
     *
     * <p>A busca prévia da ONG assegura que apenas contas com perfil de ONG
     * cadastrado recebam a listagem; caso contrário, o usuário recebe a
     * resposta padrão de recurso não encontrado.</p>
     *
     * <p>O filtro é feito no servidor pelo {@code usuarioId} da ONG — o
     * {@code ongId} informado em qualquer requisição nunca é usado como
     * critério de autorização.</p>
     */
    @Transactional(readOnly = true)
    public List<OportunidadeResponseDTO> listarOportunidadesDoUsuario(Integer usuarioId) {
        buscarPorUsuario(usuarioId);

        return oportunidadeRepository.findByOngUsuarioId(usuarioId)
                .stream()
                .map(this::paraResponseOportunidade)
                .toList();
    }

    /**
     * Converte uma oportunidade da ONG autenticada para o formato público da API.
     */
    private OportunidadeResponseDTO paraResponseOportunidade(Oportunidade oportunidade) {
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
                oportunidade.getCriadoEm()
        );
    }

    /**
     * Busca todas as ONGs cadastradas e converte cada entidade para resposta da API.
     */
    public List<OngResponseDTO> listar() {
        return ongRepository.findAll()
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    /**
     * Busca uma ONG pelo ID ou produz a resposta 404 definida no tratamento global.
     */
    public OngResponseDTO buscarPorId(Integer id) {
        return paraResponse(buscarEntidade(id));
    }

    /**
     * Cria uma ONG vinculada a um usuário já existente.
     *
     * <p>Na integração com autenticação, o {@code usuarioId} deve corresponder
     * ao perfil de ONG criado no fluxo de registro.</p>
     */
    public OngResponseDTO criar(OngRequestDTO dados) {
        Ong ong = new Ong();

        ong.setUsuarioId(dados.usuarioId());
        preencherDados(ong, dados);

        return paraResponse(ongRepository.save(ong));
    }

    /**
     * Atualiza os dados editáveis de uma ONG sem trocar o vínculo com o usuário.
     */
    public OngResponseDTO atualizar(Integer id, OngRequestDTO dados) {
        Ong ong = buscarEntidade(id);

        preencherDados(ong, dados);

        return paraResponse(ongRepository.save(ong));
    }

    /**
     * Localiza a entidade e padroniza o caso de ONG inexistente.
     */
    private Ong buscarEntidade(Integer id) {
        return ongRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ONG não encontrada."));
    }

    /**
     * Copia os campos que podem ser informados no cadastro ou na atualização.
     * O {@code usuarioId} é definido somente na criação para preservar o vínculo 1-1.
     */
    private void preencherDados(Ong ong, OngRequestDTO dados) {
        ong.setNome(dados.nome());
        ong.setCnpj(dados.cnpj());
        ong.setDescricao(dados.descricao());
        ong.setTelefone(dados.telefone());
        ong.setCidade(dados.cidade());
        ong.setEstado(dados.estado());
    }

    /**
     * Converte a entidade persistida no formato público da API.
     */
    private OngResponseDTO paraResponse(Ong ong) {
        return new OngResponseDTO(
                ong.getId(),
                ong.getNome(),
                ong.getCnpj(),
                ong.getDescricao(),
                ong.getTelefone(),
                ong.getCidade(),
                ong.getEstado()
        );
    }
}
