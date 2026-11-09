import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../services/api.js';
import './GerenciarOportunidades.css';

// "2026-10-25" -> "25/10/2026" (sem usar Date, para não deslocar o dia por fuso horário)
function formatarData(iso) {
  if (!iso) return '';
  const [ano, mes, dia] = iso.split('-');
  return `${dia}/${mes}/${ano}`;
}

const ROTULO_STATUS = { ABERTA: 'Aberta', ENCERRADA: 'Encerrada', CANCELADA: 'Cancelada' };

const FILTROS = [
  { chave: 'todas', rotulo: 'Todas' },
  { chave: 'ABERTA', rotulo: 'Abertas' },
  { chave: 'ENCERRADA', rotulo: 'Encerradas' },
  { chave: 'CANCELADA', rotulo: 'Canceladas' },
];

function GerenciarOportunidades() {
  const [oportunidades, setOportunidades] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [filtro, setFiltro] = useState('todas');
  const [cancelando, setCancelando] = useState(null);

  useEffect(() => {
    async function carregar() {
      try {
        // O backend já retorna apenas as oportunidades da ONG autenticada
        // (GET /ongs/me/oportunidades) — não é preciso filtrar no navegador.
        const minhas = await api('/ongs/me/oportunidades');
        setOportunidades(minhas);
      } catch (e) {
        setErro(e.message);
      } finally {
        setCarregando(false);
      }
    }
    carregar();
  }, []);

  const listaFiltrada = useMemo(
    () => oportunidades.filter((op) => filtro === 'todas' || op.status === filtro),
    [oportunidades, filtro]
  );

  // Cancelar não apaga a oportunidade: só muda o status para CANCELADA,
  // preservando o histórico (e os inscritos) para consulta posterior.
  async function cancelar(op) {
    if (!window.confirm(`Cancelar a oportunidade "${op.titulo}"?`)) return;
    setErro('');
    setCancelando(op.id);
    try {
      const atualizada = await api(`/oportunidades/${op.id}`, {
        method: 'PUT',
        body: {
          ongId: op.ongId,
          categoriaId: op.categoriaId,
          titulo: op.titulo,
          descricao: op.descricao,
          data: op.data,
          horario: op.horario,
          cidade: op.cidade,
          estado: op.estado,
          vagas: op.vagas,
          status: 'CANCELADA',
        },
      });
      setOportunidades((lista) => lista.map((o) => (o.id === op.id ? atualizada : o)));
    } catch (e) {
      setErro(e.message);
    } finally {
      setCancelando(null);
    }
  }

  if (carregando) return <p className="gerenciar-aviso">Carregando...</p>;

  return (
    <main className="gerenciar">
      <div className="gerenciar-topo">
        <h1>Minhas oportunidades</h1>
        <Link to="/painel/oportunidades/nova" className="gerenciar-novo">+ Criar oportunidade</Link>
      </div>

      <div className="gerenciar-filtros" role="group" aria-label="Filtrar oportunidades">
        {FILTROS.map((f) => (
          <button
            key={f.chave}
            type="button"
            className={`gerenciar-filtro ${filtro === f.chave ? 'ativo' : ''}`}
            aria-pressed={filtro === f.chave}
            onClick={() => setFiltro(f.chave)}
          >
            {f.rotulo}
          </button>
        ))}
      </div>

      {erro && <p className="gerenciar-erro" role="alert">{erro}</p>}

      {!erro && oportunidades.length === 0 && (
        <p className="gerenciar-aviso">Você ainda não publicou nenhuma oportunidade.</p>
      )}

      {!erro && oportunidades.length > 0 && listaFiltrada.length === 0 && (
        <p className="gerenciar-aviso">Nenhuma oportunidade encontrada nesse filtro.</p>
      )}

      <ul className="gerenciar-lista">
        {listaFiltrada.map((op) => (
          <li key={op.id} className="gerenciar-card">
            <Link to={`/painel/oportunidades/${op.id}/inscritos`} className="gerenciar-info" title="Ver voluntários inscritos">
              <h2>{op.titulo}</h2>
              <p>
                {op.nomeCategoria} · {formatarData(op.data)} às {(op.horario ?? '').slice(0, 5)}
              </p>
              <p>{op.cidade}{op.estado ? `/${op.estado}` : ''} · {op.vagas} vaga(s)</p>
              <span className={`gerenciar-status status-${op.status}`}>
                {ROTULO_STATUS[op.status] ?? op.status}
              </span>
            </Link>
            <div className="gerenciar-botoes">
              <Link to={`/painel/oportunidades/${op.id}/editar`} className="gerenciar-editar">Editar</Link>
              {op.status !== 'CANCELADA' && (
                <button
                  type="button"
                  className="gerenciar-excluir"
                  disabled={cancelando === op.id}
                  onClick={() => cancelar(op)}
                >
                  {cancelando === op.id ? 'Cancelando...' : 'Cancelar'}
                </button>
              )}
            </div>
          </li>
        ))}
      </ul>
    </main>
  );
}

export default GerenciarOportunidades;
