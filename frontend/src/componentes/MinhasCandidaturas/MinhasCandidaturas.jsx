import { useEffect, useMemo, useState } from 'react';
import { api } from '../../services/api.js';
import './MinhasCandidaturas.css';

// "2026-10-25" -> "25/10/2026" (sem usar Date, para não deslocar o dia por fuso horário)
function formatarData(iso) {
  if (!iso) return '';
  const [ano, mes, dia] = iso.split('-');
  return `${dia}/${mes}/${ano}`;
}

const ROTULO_STATUS = { PENDENTE: 'Inscrito', CONFIRMADA: 'Confirmado', CANCELADA: 'Cancelado' };

const FILTROS = [
  { chave: 'todas', rotulo: 'Todas' },
  { chave: 'ativas', rotulo: 'Ativas' },
  { chave: 'finalizadas', rotulo: 'Finalizadas' },
  { chave: 'canceladas', rotulo: 'Canceladas' },
];

function pertenceAoFiltro(candidatura, filtro) {
  if (filtro === 'canceladas') return candidatura.statusInscricao === 'CANCELADA';
  if (candidatura.statusInscricao === 'CANCELADA') return false;
  if (filtro === 'finalizadas') return candidatura.statusOportunidade === 'ENCERRADA';
  if (filtro === 'ativas') return candidatura.statusOportunidade === 'ABERTA';
  return true;
}

function MinhasCandidaturas() {
  const [candidaturas, setCandidaturas] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [filtro, setFiltro] = useState('todas');
  const [cancelando, setCancelando] = useState(null);
  const [expandida, setExpandida] = useState(null);

  useEffect(() => {
    async function carregar() {
      try {
        setCandidaturas(await api('/inscricoes/me'));
      } catch (e) {
        setErro(e.message);
      } finally {
        setCarregando(false);
      }
    }
    carregar();
  }, []);

  const listaFiltrada = useMemo(
    () => candidaturas.filter((c) => pertenceAoFiltro(c, filtro)),
    [candidaturas, filtro]
  );

  async function cancelar(candidatura) {
    if (!window.confirm(`Cancelar sua inscrição em "${candidatura.tituloOportunidade}"?`)) return;
    setErro('');
    setCancelando(candidatura.id);
    try {
      await api(`/inscricoes/${candidatura.id}`, { method: 'DELETE' });
      setCandidaturas((lista) =>
        lista.map((c) => (c.id === candidatura.id ? { ...c, statusInscricao: 'CANCELADA' } : c))
      );
    } catch (e) {
      setErro(e.message);
    } finally {
      setCancelando(null);
    }
  }

  if (carregando) return <p className="candidaturas-aviso">Carregando...</p>;

  return (
    <main className="candidaturas">
      <h1>Minhas candidaturas</h1>

      <div className="candidaturas-filtros">
        {FILTROS.map((f) => (
          <button
            key={f.chave}
            type="button"
            className={`candidaturas-filtro ${filtro === f.chave ? 'ativo' : ''}`}
            onClick={() => setFiltro(f.chave)}
          >
            {f.rotulo}
          </button>
        ))}
      </div>

      {erro && <p className="candidaturas-erro" role="alert">{erro}</p>}

      {!erro && listaFiltrada.length === 0 && (
        <p className="candidaturas-aviso">Nenhuma candidatura encontrada nesse filtro.</p>
      )}

      <ul className="candidaturas-lista">
        {listaFiltrada.map((c) => (
          <li key={c.id} className="candidaturas-card">
            <div className="candidaturas-info">
              <h2>{c.tituloOportunidade}</h2>
              <p>ONG: {c.nomeOng}</p>
              <p>Data: {formatarData(c.data)}{c.horario ? ` às ${c.horario.slice(0, 5)}` : ''}</p>
              <span className={`candidaturas-status status-${c.statusInscricao}`}>
                {ROTULO_STATUS[c.statusInscricao] ?? c.statusInscricao}
              </span>
              {expandida === c.id && (
                <p className="candidaturas-extra">
                  {c.cidade}{c.estado ? `/${c.estado}` : ''}
                  {c.descricaoOportunidade ? ` · ${c.descricaoOportunidade}` : ''}
                </p>
              )}
            </div>
            <div className="candidaturas-botoes">
              <button
                type="button"
                className="candidaturas-detalhes"
                onClick={() => setExpandida((atual) => (atual === c.id ? null : c.id))}
              >
                {expandida === c.id ? 'Ocultar detalhes' : 'Ver detalhes'}
              </button>
              {c.statusInscricao !== 'CANCELADA' && (
                <button
                  type="button"
                  className="candidaturas-cancelar"
                  disabled={cancelando === c.id}
                  onClick={() => cancelar(c)}
                >
                  {cancelando === c.id ? 'Cancelando...' : 'Cancelar'}
                </button>
              )}
            </div>
          </li>
        ))}
      </ul>
    </main>
  );
}

export default MinhasCandidaturas;
