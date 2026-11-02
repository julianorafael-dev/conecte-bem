import { useState, useMemo } from 'react';
import { FaSearch, FaTimes } from 'react-icons/fa';
import './Oportunidade.css';
import vagas from '../../mocks/vagasMock.json';

function Oportunidades() {
  const [busca, setBusca] = useState('');
  const [categoria, setCategoria] = useState('');
  const [ongSelecionada, setOngSelecionada] = useState('');
  const [somenteAbertas, setSomenteAbertas] = useState(false);
  const [cidadesSelecionadas, setCidadesSelecionadas] = useState([]);
  
  // Estado para controlar a abertura do Modal
  const [vagaSelecionada, setVagaSelecionada] = useState(null);

  const ongsUnicas = useMemo(() => [...new Set(vagas.map(v => v.ong.nome))], []);
  const localizacoesUnicas = useMemo(() => [...new Set(vagas.map(v => v.localizacao))], []);

  const handleCidadeChange = (cidade) => {
    setCidadesSelecionadas((prev) => 
      prev.includes(cidade) 
        ? prev.filter((c) => c !== cidade) 
        : [...prev, cidade]
    );
  };

  const vagasFiltradas = vagas.filter((vaga) => {
    const tituloMatch = vaga.titulo.toLowerCase().includes(busca.toLowerCase());
    const categoriaMatch = categoria === '' || vaga.categoria === categoria;
    const ongMatch = ongSelecionada === '' || vaga.ong.nome === ongSelecionada;
    const statusMatch = !somenteAbertas || vaga.status === 'ABERTA';
    const cidadeMatch = cidadesSelecionadas.length === 0 || cidadesSelecionadas.includes(vaga.localizacao);
    
    return tituloMatch && categoriaMatch && ongMatch && statusMatch && cidadeMatch;
  });

  return (
    <div className="oportunidades-page">
      <h1>Oportunidades de Voluntariado</h1>
      <p className="oportunidades-subtitle">Encontre a causa perfeita para você e faça a diferença.</p>

      <div className="oportunidades-search-wrapper">
        <div className="oportunidades-search">
          <span className="oportunidades-search-icon">
            <FaSearch />
          </span>
          <input
            type="text"
            placeholder="Buscar oportunidades por título ou palavra-chave..."
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
          />
        </div>
      </div>

      <div className="oportunidades-layout">
        <aside className="oportunidades-filtros">
          <h3>Filtros</h3>

          <div className="oportunidades-filtro-group">
            <label className="oportunidades-filtro-label">Causa</label>
            <select className="oportunidades-select" value={categoria} onChange={(e) => setCategoria(e.target.value)}>
              <option value="">Todas as causas</option>
              <option value="Educação">Educação</option>
              <option value="Meio Ambiente">Meio Ambiente</option>
              <option value="Tecnologia">Tecnologia</option>
            </select>
          </div>

          <div className="oportunidades-filtro-group">
            <label className="oportunidades-filtro-label">ONG</label>
            <select className="oportunidades-select" value={ongSelecionada} onChange={(e) => setOngSelecionada(e.target.value)}>
              <option value="">Todas as ONGs</option>
              {ongsUnicas.map((ong) => (
                <option key={ong} value={ong}>{ong}</option>
              ))}
            </select>
          </div>

          <label className="oportunidades-checkbox">
            <input
              type="checkbox"
              checked={somenteAbertas}
              onChange={(e) => setSomenteAbertas(e.target.checked)}
            />
            Somente Vagas Abertas
          </label>

          <div className="oportunidades-localizacao">
            <label className="oportunidades-filtro-label">Localização</label>
            <div className="oportunidades-localizacao-lista">
              {localizacoesUnicas.map((loc) => (
                <label key={loc} className="oportunidades-localizacao-item">
                  <input
                    type="checkbox"
                    checked={cidadesSelecionadas.includes(loc)}
                    onChange={() => handleCidadeChange(loc)}
                  />
                  {loc}
                </label>
              ))}
            </div>
          </div>
        </aside>

        <main className="oportunidades-main">
          <div className="oportunidades-resultado">
            {vagasFiltradas.length} {vagasFiltradas.length === 1 ? 'oportunidade encontrada' : 'oportunidades encontradas'}
          </div>

          {vagasFiltradas.length > 0 ? (
            vagasFiltradas.map((vaga) => (
              <div key={vaga.id} className="oportunidades-card">
                <div className="oportunidades-card-header">
                  <h2 className="oportunidades-card-title">{vaga.titulo}</h2>
                  <span className={`oportunidades-status ${vaga.status === 'ABERTA' ? 'aberta' : 'fechada'}`}>
                    {vaga.status}
                  </span>
                </div>
                <p className="oportunidades-card-meta">
                  <strong>{vaga.ong.nome}</strong> • {vaga.categoria} • {vaga.localizacao}
                </p>
                <p className="oportunidades-card-description">{vaga.descricao}</p>

                <div className="oportunidades-card-footer">
                  <span className={`oportunidades-vagas ${vaga.vagasDisponiveis > 0 ? 'disponivel' : 'indisponivel'}`}>
                    {vaga.vagasDisponiveis} vagas disponíveis de {vaga.vagasTotais}
                  </span>
                  <button 
                    className="oportunidades-button"
                    onClick={() => setVagaSelecionada(vaga)} 
                  >
                    Ver Detalhes
                  </button>
                </div>
              </div>
            ))
          ) : (
            <div className="oportunidades-empty">
              <h3>Nenhuma oportunidade encontrada</h3>
              <p>Tente ajustar os filtros ao lado ou pesquisar por outros termos.</p>
            </div>
          )}
        </main>
      </div>

      {/* MODAL SOBREPOSTO */}
      {vagaSelecionada && (
        <div className="modal-overlay" onClick={() => setVagaSelecionada(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setVagaSelecionada(null)}>
              <FaTimes />
            </button>

            <h2 className="modal-title">{vagaSelecionada.titulo}</h2>
            <p className="modal-ong"><strong>{vagaSelecionada.ong.nome}</strong></p>
            
            <div className="modal-tags">
              <span className="modal-tag">{vagaSelecionada.categoria}</span>
              <span className="modal-tag">{vagaSelecionada.localizacao}</span>
              <span className={`modal-tag modal-tag-status ${vagaSelecionada.status === 'ABERTA' ? 'aberta' : 'fechada'}`}>
                {vagaSelecionada.status}
              </span>
            </div>

            <h3>Descrição da Atividade</h3>
            <p className="modal-description">{vagaSelecionada.descricao}</p>
            
            <div className="modal-action-box">
              <p className="modal-vagas-info">
                {vagaSelecionada.vagasDisponiveis} de {vagaSelecionada.vagasTotais} vagas disponíveis
              </p>
              
              <button 
                className={`modal-btn-inscrever ${vagaSelecionada.status !== 'ABERTA' || vagaSelecionada.vagasDisponiveis === 0 ? 'esgotada' : ''}`}
                disabled={vagaSelecionada.status !== 'ABERTA' || vagaSelecionada.vagasDisponiveis === 0}
                onClick={() => alert(`Inscrição simulada para: ${vagaSelecionada.titulo}.`)}
              >
                {vagaSelecionada.status !== 'ABERTA' || vagaSelecionada.vagasDisponiveis === 0 ? 'Vagas Esgotadas' : 'Quero me Inscrever'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Oportunidades;