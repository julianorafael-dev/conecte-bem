package com.conectebem.backend.service;

import com.conectebem.backend.dto.OngRequestDTO;
import com.conectebem.backend.dto.OngResponseDTO;
import com.conectebem.backend.exception.ResourceNotFoundException;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.repository.OngRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Contém as regras de negócio do módulo ONG.
 *
 * <p>Esta camada recebe dados do controller, usa o repositório para acessar o
 * banco e devolve DTOs para evitar expor a entidade diretamente pela API.</p>
 */
@Service
public class OngService {

    private final OngRepository ongRepository;

    public OngService(OngRepository ongRepository) {
        this.ongRepository = ongRepository;
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
