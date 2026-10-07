package com.conectebem.backend.service;

import com.conectebem.backend.dto.OportunidadeRequestDTO;
import com.conectebem.backend.dto.OportunidadeResponseDTO;
import com.conectebem.backend.exception.ResourceNotFoundException;
import com.conectebem.backend.model.Categoria;
import com.conectebem.backend.model.Ong;
import com.conectebem.backend.model.Oportunidade;
import com.conectebem.backend.repository.CategoriaRepository;
import com.conectebem.backend.repository.OngRepository;
import com.conectebem.backend.repository.OportunidadeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class OportunidadeService {

    private final OportunidadeRepository oportunidadeRepository;
    private final CategoriaRepository categoriaRepository;
    private final OngRepository ongRepository;

    public OportunidadeService(OportunidadeRepository oportunidadeRepository,
                               CategoriaRepository categoriaRepository,
                               OngRepository ongRepository) {
        this.oportunidadeRepository = oportunidadeRepository;
        this.categoriaRepository = categoriaRepository;
        this.ongRepository = ongRepository;
    }

    /** Lista todas as oportunidades cadastradas e converte para DTO. */
    public List<OportunidadeResponseDTO> listar() {
        return oportunidadeRepository.findAll().stream()
                .map(this::paraResponse)
                .toList();
    }

    /** Busca uma oportunidade pelo ID ou lança ResourceNotFoundException. */
    public OportunidadeResponseDTO buscarPorId(Integer id) {
        Oportunidade oportunidade = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada. ID: " + id));
        return paraResponse(oportunidade);
    }

    /** Cria uma nova oportunidade a partir dos dados do RequestDTO. */
    public OportunidadeResponseDTO criar(OportunidadeRequestDTO dados) {
        // 1. Buscar ONG pelo ongId
        Integer ongId = dados.ongId();
        Ong ong = ongRepository.findById(ongId)
                .orElseThrow(() -> new ResourceNotFoundException("ONG não encontrada. ID: " + ongId));

        // 2. Buscar Categoria pelo categoriaId
        Integer categoriaId = dados.categoriaId();
        Categoria categoria = categoriaRepository.findById(Long.valueOf(categoriaId))
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

        // 4. Preencher criadoEm com data atual (formato String yyyy-MM-dd)
        oportunidade.setCriadoEm(LocalDate.now().toString());

        // 5. Salvar no repositório
        oportunidade = oportunidadeRepository.save(oportunidade);

        // 6. Retornar DTO de resposta
        return paraResponse(oportunidade);
    }

    /** Atualiza uma oportunidade existente. */
    public OportunidadeResponseDTO atualizar(Integer id, OportunidadeRequestDTO dados) {
        // 1. Verificar se a oportunidade existe
        Oportunidade oportunidadeExistente = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada. ID: " + id));

        // 2. Buscar ONG e Categoria (validar existência)
        Integer ongId = dados.ongId();
        Ong ong = ongRepository.findById(ongId)
                .orElseThrow(() -> new ResourceNotFoundException("ONG não encontrada. ID: " + ongId));

        Integer categoriaId = dados.categoriaId();
        Categoria categoria = categoriaRepository.findById(Long.valueOf(categoriaId))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada. ID: " + categoriaId));

        // 3. Atualizar campos permitidos
        // NÃO alterar o ID
        // NÃO alterar criadoEm

        oportunidadeExistente.setOng(ong);
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

    /** Exclui uma oportunidade pelo ID. */
    public void excluir(Integer id) {
        // Verificar se existe antes de excluir
        if (!oportunidadeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Oportunidade não encontrada. ID: " + id);
        }
        oportunidadeRepository.deleteById(id);
    }

    /** Filtra oportunidades por cidade. */
    public List<OportunidadeResponseDTO> filtrarPorCidade(String cidade) {
        return oportunidadeRepository.findByCidade(cidade).stream()
                .map(this::paraResponse)
                .toList();
    }

    /** Filtra oportunidades pela categoria relacionada. */
    public List<OportunidadeResponseDTO> filtrarPorCategoria(Integer categoriaId) {
        Categoria categoria = categoriaRepository.findById(Long.valueOf(categoriaId))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada. ID: " + categoriaId));
        return oportunidadeRepository.findByCategoria(categoria).stream()
                .map(this::paraResponse)
                .toList();
    }

    /** Filtra oportunidades por status. */
    public List<OportunidadeResponseDTO> filtrarPorStatus(String status) {
        return oportunidadeRepository.findByStatus(status).stream()
                .map(this::paraResponse)
                .toList();
    }

    /** Converte entidade Oportunidade para DTO de resposta. */
    private OportunidadeResponseDTO paraResponse(Oportunidade oportunidade) {
        return new OportunidadeResponseDTO(
                oportunidade.getId(),
                oportunidade.getOng().getId(),
                oportunidade.getOng().getNome(),
                oportunidade.getCategoria().getId().intValue(), // Categoria.id é Long; DTO espera Integer (ver nota sobre padronizar tipos)
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
}
