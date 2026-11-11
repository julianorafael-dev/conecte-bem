import { useState, useEffect, useMemo, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { FaSearch, FaTimes } from 'react-icons/fa';
import { api } from '../../services/api.js';
import { getUsuario } from '../../services/auth.js';
import './Oportunidade.css';

// "2026-10-25" -> "25/10/2026" (sem usar Date, para não deslocar o dia por fuso horário)
function formatarData(iso) {
  if (!iso) return 'Data a definir';
  const [ano, mes, dia] = iso.split('-');
  return `${dia}/${mes}/${ano}`;
}

// "14:00:00" -> "14:00"
function formatarHorario(horario) {
  return horario ? horario.slice(0, 5) : 'Horário a definir';
}

// Cidade e estado podem vir nulos no backend; exibe apenas o que existir.
function formatarLocalizacao(oportunidade) {
  const cidade = oportunidade.cidade?.trim();
  const estado = oportunidade.estado?.trim();
  if (cidade && estado) return `${cidade}/${estado}`;
  return cidade || estado || 'Local a definir';
}

const ROTULO_STATUS = { ABERTA: 'Aberta', ENCERRADA: 'Encerrada', CANCELADA: 'Cancelada' };

const MENSAGEM_ERRO_CATEGORIAS =
    'Não foi possível carregar as categorias. O filtro por categoria está indisponível.';

function Oportunidades() {
  const [oportunidades, setOportunidades] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [erroCategorias, setErroCategorias] = useState('');
  const [carregandoCategorias, setCarregandoCategorias] = useState(false);

  // Filtros (aplicados em memória sobre a listagem pública)
  const [busca, setBusca] = useState('');
  const [categoriaId, setCategoriaId] = useState('');
  const [ongSelecionada, setOngSelecionada] = useState('');
  const [somenteAbertas, setSomenteAbertas] = useState(false);
  const [localidadesSelecionadas, setLocalidadesSelecionadas] = useState([]);

  // Modal e inscrição
  const [oportunidadeSelecionada, setOportunidadeSelecionada] = useState(null);
  const [inscritasNestaSessao, setInscritasNestaSessao] = useState([]);
  const [carregandoInscricao, setCarregandoInscricao] = useState(false);
  const [erroInscricao, setErroInscricao] = useState('');

  const carregarCategorias = useCallback(async () => {
    setCarregandoCategorias(true);
    try {
      const lista = await api('/categorias');
      setCategorias(lista ?? []);
      setErroCategorias('');
    } catch {
      setErroCategorias(MENSAGEM_ERRO_CATEGORIAS);
    } finally {
      setCarregandoCategorias(false);
    }
  }, []);

  useEffect(() => {
    let ativo = true;

    async function carregarOportunidades() {
      try {
        const lista = await api('/oportunidades');
        // Oportunidades canceladas pela ONG não aparecem na listagem pública.
        if (ativo) setOportunidades((lista ?? []).filter((o) => o.status !== 'CANCELADA'));
      } catch (error) {
        if (ativo) {
          setErro(
              error.message ||
              'Não foi possível carregar as oportunidades. Tente novamente.'
          );
        }
      } finally {
        if (ativo) setCarregando(false);
      }
    }

    async function carregarCategoriasIniciais() {
      try {
        const lista = await api('/categorias');
        if (!ativo) return;
        setCategorias(lista ?? []);
      } catch {
        if (ativo) setErroCategorias(MENSAGEM_ERRO_CATEGORIAS);
      }
    }

    // A listagem de oportunidades é essencial; as categorias apenas alimentam
    // o filtro. Se o endpoint de categorias falhar, a página continua útil.
    carregarOportunidades();
    carregarCategoriasIniciais();

    return () => {
      ativo = false;
    };
  }, []);

  // Valores únicos para os filtros, derivados dos dados reais da API.
  const ongsUnicas = useMemo(
      () => [...new Set(oportunidades.map((o) => o.nomeOng).filter(Boolean))],
      [oportunidades]
  );
  const localidadesUnicas = useMemo(
      () => [...new Set(oportunidades.map(formatarLocalizacao))],
      [oportunidades]
  );

  const alternarLocalidade = (localidade) => {
    setLocalidadesSelecionadas((atuais) =>
        atuais.includes(localidade)
            ? atuais.filter((item) => item !== localidade)
            : [...atuais, localidade]
    );
  };

  const oportunidadesFiltradas = oportunidades.filter((oportunidade) => {
    const texto = busca.trim().toLowerCase();
    const buscaMatch =
        texto === '' ||
        (oportunidade.titulo ?? '').toLowerCase().includes(texto) ||
        (oportunidade.descricao ?? '').toLowerCase().includes(texto);

    const categoriaMatch =
        categoriaId === '' || String(oportunidade.categoriaId) === categoriaId;
    const ongMatch = ongSelecionada === '' || oportunidade.nomeOng === ongSelecionada;
    const statusMatch = !somenteAbertas || oportunidade.status === 'ABERTA';
    const localidadeMatch =
        localidadesSelecionadas.length === 0 ||
        localidadesSelecionadas.includes(formatarLocalizacao(oportunidade));

    return buscaMatch && categoriaMatch && ongMatch && statusMatch && localidadeMatch;
  });

  async function inscrever() {
    const autenticado = getUsuario();

    if (!autenticado) {
      setErroInscricao('Você precisa entrar na sua conta para se inscrever.');
      return;
    }
    if (autenticado.tipo === 'ONG') {
      setErroInscricao('Contas de ONG não podem se inscrever em oportunidades.');
      return;
    }

    setCarregandoInscricao(true);
    setErroInscricao('');

    try {
      await api('/inscricoes', {
        method: 'POST',
        body: { oportunidadeId: oportunidadeSelecionada.id },
      });
      setInscritasNestaSessao((anteriores) => [...anteriores, oportunidadeSelecionada.id]);
    } catch (error) {
      setErroInscricao(error.message || 'Erro ao realizar inscrição. Tente novamente.');
    } finally {
      setCarregandoInscricao(false);
    }
  }

  if (carregando) {
    return (
        <div className="oportunidades-page" style={{ textAlign: 'center', padding: '4rem' }}>
          <h2 style={{ color: '#666' }}>Carregando oportunidades...</h2>
        </div>
    );
  }

  if (erro) {
    return (
        <div className="oportunidades-page" style={{ textAlign: 'center', padding: '4rem' }}>
          <h2 style={{ color: '#d9534f' }}>Não foi possível carregar as oportunidades</h2>
          <p>{erro}</p>
          <button
              onClick={() => window.location.reload()}
              className="oportunidades-button"
              style={{ marginTop: '1rem' }}
          >
            Tentar novamente
          </button>
        </div>
    );
  }

  const usuarioAtual = getUsuario();
  const podeInscrever = usuarioAtual && usuarioAtual.tipo === 'VOLUNTARIO';

  return (
      <div className="oportunidades-page">
        <h1>Oportunidades de Voluntariado</h1>
        <p className="oportunidades-subtitle">
          Encontre a causa perfeita para você e faça a diferença.
        </p>

        <div className="oportunidades-search-wrapper">
          <div className="oportunidades-search">
          <span className="oportunidades-search-icon">
            <FaSearch />
          </span>
            {/* Correção QA-018: Inserido o aria-label */}
            <input
                type="text"
                placeholder="Buscar oportunidades por título ou palavra-chave..."
                aria-label="Buscar oportunidades"
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
            />
          </div>
        </div>

        <div className="oportunidades-layout">
          <aside className="oportunidades-filtros">
            <h3>Filtros</h3>

            <div className="oportunidades-filtro-group">
              <label className="oportunidades-filtro-label" htmlFor="filtro-categoria">
                Categoria
              </label>
              <select
                  id="filtro-categoria"
                  className="oportunidades-select"
                  value={categoriaId}
                  onChange={(e) => setCategoriaId(e.target.value)}
                  disabled={!!erroCategorias || carregandoCategorias}
              >
                <option value="">Todas as categorias</option>
                {categorias.map((categoria) => (
                    <option key={categoria.id} value={String(categoria.id)}>
                      {categoria.nome}
                    </option>
                ))}
              </select>

              {erroCategorias && (
                  <div className="oportunidades-filtro-aviso-categorias">
                    <p role="alert">{erroCategorias}</p>
                    <button
                        type="button"
                        className="oportunidades-filtro-retry"
                        onClick={carregarCategorias}
                        disabled={carregandoCategorias}
                    >
                      {carregandoCategorias ? 'Tentando novamente...' : 'Tentar novamente'}
                    </button>
                  </div>
              )}
            </div>

            <div className="oportunidades-filtro-group">
              <label className="oportunidades-filtro-label" htmlFor="filtro-ong">
                ONG
              </label>
              <select
                  id="filtro-ong"
                  className="oportunidades-select"
                  value={ongSelecionada}
                  onChange={(e) => setOngSelecionada(e.target.value)}
              >
                <option value="">Todas as ONGs</option>
                {ongsUnicas.map((nome) => (
                    <option key={nome} value={nome}>
                      {nome}
                    </option>
                ))}
              </select>
            </div>

            <label className="oportunidades-checkbox">
              <input
                  type="checkbox"
                  checked={somenteAbertas}
                  onChange={(e) => setSomenteAbertas(e.target.checked)}
              />
              Somente vagas abertas
            </label>

            <div className="oportunidades-localizacao">
              <span className="oportunidades-filtro-label">Localização</span>
              {localidadesUnicas.length === 0 ? (
                  <p style={{ color: '#666', fontSize: '0.9rem' }}>Nenhuma localidade cadastrada.</p>
              ) : (
                  <div className="oportunidades-localizacao-lista">
                    {localidadesUnicas.map((localidade) => (
                        <label key={localidade} className="oportunidades-localizacao-item">
                          <input
                              type="checkbox"
                              checked={localidadesSelecionadas.includes(localidade)}
                              onChange={() => alternarLocalidade(localidade)}
                          />
                          {localidade}
                        </label>
                    ))}
                  </div>
              )}
            </div>
          </aside>

          <main className="oportunidades-main">
            <div className="oportunidades-resultado">
              {oportunidadesFiltradas.length}{' '}
              {oportunidadesFiltradas.length === 1
                  ? 'oportunidade encontrada'
                  : 'oportunidades encontradas'}
            </div>

            {oportunidadesFiltradas.length > 0 ? (
                oportunidadesFiltradas.map((oportunidade) => (
                    <div key={oportunidade.id} className="oportunidades-card">
                      <div className="oportunidades-card-header">
                        <h2 className="oportunidades-card-title">{oportunidade.titulo}</h2>
                        <span
                            className={`oportunidades-status ${
                                oportunidade.status === 'ABERTA' ? 'aberta' : 'fechada'
                            }`}
                        >
                    {ROTULO_STATUS[oportunidade.status] ?? oportunidade.status}
                  </span>
                      </div>
                      <p className="oportunidades-card-meta">
                        <strong>{oportunidade.nomeOng}</strong> • {oportunidade.nomeCategoria} •{' '}
                        {formatarLocalizacao(oportunidade)}
                      </p>
                      <p className="oportunidades-card-description">
                        {oportunidade.descricao || 'Sem descrição informada.'}
                      </p>

                      <div className="oportunidades-card-footer">
                  <span className="oportunidades-vagas">
                    Capacidade: {oportunidade.vagas}{' '}
                    {oportunidade.vagas === 1 ? 'vaga' : 'vagas'}
                  </span>
                        <button
                            className="oportunidades-button"
                            onClick={() => {
                              setOportunidadeSelecionada(oportunidade);
                              setErroInscricao('');
                            }}
                        >
                          Ver detalhes
                        </button>
                      </div>
                    </div>
                ))
            ) : (
                <div className="oportunidades-empty">
                  {oportunidades.length === 0 ? (
                      <>
                        <h3>Nenhuma oportunidade disponível no momento</h3>
                        <p>Novas oportunidades serão publicadas em breve. Volte mais tarde!</p>
                      </>
                  ) : (
                      <>
                        <h3>Nenhuma oportunidade encontrada</h3>
                        <p>Tente ajustar os filtros ao lado ou pesquisar por outros termos.</p>
                      </>
                  )}
                </div>
            )}
          </main>
        </div>

        {oportunidadeSelecionada && (
            <div className="modal-overlay" onClick={() => setOportunidadeSelecionada(null)}>
              <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <button
                    type="button"
                    className="modal-close"
                    aria-label="Fechar detalhes"
                    onClick={() => setOportunidadeSelecionada(null)}
                >
                  <FaTimes />
                </button>

                <h2 className="modal-title">{oportunidadeSelecionada.titulo}</h2>
                <p className="modal-ong">
                  <strong>{oportunidadeSelecionada.nomeOng}</strong>
                </p>

                <div className="modal-tags">
                  <span className="modal-tag">{oportunidadeSelecionada.nomeCategoria}</span>
                  <span className="modal-tag">{formatarLocalizacao(oportunidadeSelecionada)}</span>
                  <span
                      className={`modal-tag modal-tag-status ${
                          oportunidadeSelecionada.status === 'ABERTA' ? 'aberta' : 'fechada'
                      }`}
                  >
                {ROTULO_STATUS[oportunidadeSelecionada.status] ?? oportunidadeSelecionada.status}
              </span>
                </div>

                <h3>Descrição da atividade</h3>
                <p className="modal-description">
                  {oportunidadeSelecionada.descricao || 'Sem descrição informada.'}
                </p>

                <p className="modal-vagas-info">
                  {formatarData(oportunidadeSelecionada.data)} às{' '}
                  {formatarHorario(oportunidadeSelecionada.horario)}
                </p>

                <div className="modal-action-box">
                  <p className="modal-vagas-info">
                    Capacidade: {oportunidadeSelecionada.vagas}{' '}
                    {oportunidadeSelecionada.vagas === 1 ? 'vaga' : 'vagas'}
                  </p>

                  {erroInscricao && (
                      <div
                          role="alert"
                          style={{
                            padding: '1rem',
                            backgroundColor: '#f8d7da',
                            color: '#721c24',
                            borderRadius: '8px',
                            marginBottom: '1rem',
                            fontWeight: 'bold',
                          }}
                      >
                        {erroInscricao}
                      </div>
                  )}

                  {inscritasNestaSessao.includes(oportunidadeSelecionada.id) && (
                      <div
                          role="status"
                          style={{
                            padding: '1rem',
                            backgroundColor: '#d4edda',
                            color: '#155724',
                            borderRadius: '8px',
                            marginBottom: '1rem',
                            fontWeight: 'bold',
                          }}
                      >
                        Inscrição realizada com sucesso!
                      </div>
                  )}

                  {!usuarioAtual && (
                      <>
                        <p className="modal-vagas-info">
                          Entre na sua conta para se inscrever nesta oportunidade.
                        </p>
                        <Link to="/entrar" className="modal-btn-inscrever">
                          Entrar para se inscrever
                        </Link>
                      </>
                  )}

                  {usuarioAtual && !podeInscrever && (
                      <p className="modal-vagas-info">
                        Contas de ONG não podem se inscrever em oportunidades.
                      </p>
                  )}

                  {podeInscrever && (
                      <button
                          type="button"
                          className={`modal-btn-inscrever ${
                              oportunidadeSelecionada.status !== 'ABERTA' ||
                              inscritasNestaSessao.includes(oportunidadeSelecionada.id)
                                  ? 'esgotada'
                                  : ''
                          }`}
                          disabled={
                              oportunidadeSelecionada.status !== 'ABERTA' ||
                              inscritasNestaSessao.includes(oportunidadeSelecionada.id) ||
                              carregandoInscricao
                          }
                          onClick={inscrever}
                      >
                        {inscritasNestaSessao.includes(oportunidadeSelecionada.id)
                            ? 'Inscrito'
                            : carregandoInscricao
                                ? 'Processando...'
                                : oportunidadeSelecionada.status !== 'ABERTA'
                                    ? 'Inscrições encerradas'
                                    : 'Quero me inscrever'}
                      </button>
                  )}
                </div>
              </div>
            </div>
        )}
      </div>
  );
}

export default Oportunidades;