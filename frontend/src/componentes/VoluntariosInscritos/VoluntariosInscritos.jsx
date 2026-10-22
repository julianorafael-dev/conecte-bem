import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../../services/api.js';
import './VoluntariosInscritos.css';

const ROTULO_STATUS = { PENDENTE: 'Pendente', CONFIRMADA: 'Confirmada', CANCELADA: 'Cancelada' };

// Tela da ONG com a lista de voluntários inscritos em uma oportunidade (/painel/oportunidades/:id/inscritos)
function VoluntariosInscritos() {
  const { id } = useParams();

  const [oportunidade, setOportunidade] = useState(null);
  const [inscricoes, setInscricoes] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    async function carregar() {
      try {
        const [minhaOng, op] = await Promise.all([
          api('/ongs/me'),
          api(`/oportunidades/${id}`),
        ]);

        if (op.ongId !== minhaOng.id) {
          setErro('Você só pode ver os inscritos de oportunidades da sua própria ONG.');
          return;
        }

        setOportunidade(op);
        setInscricoes(await api(`/oportunidades/${id}/inscricoes`));
      } catch (e) {
        setErro(e.message);
      } finally {
        setCarregando(false);
      }
    }
    carregar();
  }, [id]);

  if (carregando) return <p className="gerenciar-aviso">Carregando...</p>;

  return (
    <main className="inscritos">
      <Link to="/painel/oportunidades" className="inscritos-voltar">← Voltar para minhas oportunidades</Link>

      {erro && <p className="gerenciar-erro" role="alert">{erro}</p>}

      {oportunidade && (
        <>
          <section className="inscritos-oportunidade">
            <h1>{oportunidade.titulo}</h1>
            <p>{oportunidade.descricao}</p>
          </section>

          <h2 className="inscritos-titulo">Voluntários Inscritos</h2>

          {!erro && inscricoes.length === 0 && (
            <p className="gerenciar-aviso">Nenhum voluntário se inscreveu nesta oportunidade ainda.</p>
          )}

          <ul className="inscritos-lista">
            {inscricoes.map((inscricao) => (
              <li key={inscricao.id} className="inscritos-card">
                <div>
                  <strong>{inscricao.nomeVoluntario}</strong>
                  <p>
                    <a href={`mailto:${inscricao.emailVoluntario}`} className="inscritos-email">
                      {inscricao.emailVoluntario}
                    </a>
                  </p>
                </div>
                <span className={`inscritos-status inscricao-${inscricao.status}`}>
                  {ROTULO_STATUS[inscricao.status] ?? inscricao.status}
                </span>
              </li>
            ))}
          </ul>
        </>
      )}
    </main>
  );
}

export default VoluntariosInscritos;
