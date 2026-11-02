import { useState, useMemo } from 'react';
import { FaSearch } from 'react-icons/fa';
import './Oportunidade.css';
import vagas from '../../mocks/vagasMock.json';

function Oportunidades() {
  const [busca, setBusca] = useState('');
  const [categoria, setCategoria] = useState('');
  const [ongSelecionada, setOngSelecionada] = useState('');
  const [somenteAbertas, setSomenteAbertas] = useState(false);
  const [cidadesSelecionadas, setCidadesSelecionadas] = useState([]);

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
                  <button className="oportunidades-button">Ver Detalhes</button>
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
    </div>
  );
}

export default Oportunidades;