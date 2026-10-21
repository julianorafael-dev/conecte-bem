import { useEffect, useState } from 'react';
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

function GerenciarOportunidades() {
  const [oportunidades, setOportunidades] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    async function carregar() {
      try {
        const minhaOng = await api('/ongs/me');
        const todas = await api('/oportunidades');
        setOportunidades(todas.filter((op) => op.ongId === minhaOng.id));
      } catch (e) {
        setErro(e.message);
      } finally {
        setCarregando(false);
      }
    }
    carregar();
  }, []);

  async function excluir(op) {
    if (!window.confirm(`Excluir a oportunidade "${op.titulo}"? Essa ação não pode ser desfeita.`)) return;
    setErro('');
    try {
      await api(`/oportunidades/${op.id}`, { method: 'DELETE' });
      setOportunidades((lista) => lista.filter((o) => o.id !== op.id));
    } catch (e) {
      setErro(e.message);
    }
  }

  if (carregando) return <p className="gerenciar-aviso">Carregando...</p>;

  return (
    <main className="gerenciar">
      <div className="gerenciar-topo">
        <h1>Minhas oportunidades</h1>
        <Link to="/painel/oportunidades/nova" className="gerenciar-novo">+ Criar oportunidade</Link>
      </div>

      {erro && <p className="gerenciar-erro" role="alert">{erro}</p>}

      {!erro && oportunidades.length === 0 && (
        <p className="gerenciar-aviso">Você ainda não publicou nenhuma oportunidade.</p>
      )}

      <ul className="gerenciar-lista">
        {oportunidades.map((op) => (
          <li key={op.id} className="gerenciar-card">
            <div>
              <h2>{op.titulo}</h2>
              <p>
                {op.nomeCategoria} · {formatarData(op.data)} às {(op.horario ?? '').slice(0, 5)}
              </p>
              <p>{op.cidade}{op.estado ? `/${op.estado}` : ''} · {op.vagas} vaga(s)</p>
              <span className={`gerenciar-status status-${op.status}`}>
                {ROTULO_STATUS[op.status] ?? op.status}
              </span>
            </div>
            <div className="gerenciar-botoes">
              <Link to={`/painel/oportunidades/${op.id}/editar`} className="gerenciar-editar">Editar</Link>
              <button type="button" className="gerenciar-excluir" onClick={() => excluir(op)}>Excluir</button>
            </div>
          </li>
        ))}
      </ul>
    </main>
  );
}

export default GerenciarOportunidades;
