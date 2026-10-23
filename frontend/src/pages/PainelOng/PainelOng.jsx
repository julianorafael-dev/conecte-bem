import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../services/api.js';
import './PainelOng.css';

const ROTULO_STATUS = {
  ABERTA: 'Aberta',
  ENCERRADA: 'Encerrada',
  CANCELADA: 'Cancelada',
};

function formatarData(data) {
  if (!data) return '';
  const [ano, mes, dia] = data.split('-');
  return `${dia}/${mes}/${ano}`;
}

function PainelOng() {
  const [resumo, setResumo] = useState(null);
  const [oportunidades, setOportunidades] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    let ativo = true;

    async function carregarPainel() {
      try {
        const [dadosPainel, minhaOng, todasOportunidades] = await Promise.all([
          api('/ongs/me/painel'),
          api('/ongs/me'),
          api('/oportunidades'),
        ]);

        const minhasOportunidades = todasOportunidades
          .filter((oportunidade) => oportunidade.ongId === minhaOng.id)
          .sort((primeira, segunda) => new Date(segunda.criadoEm) - new Date(primeira.criadoEm))
          .slice(0, 3);

        if (ativo) {
          setResumo(dadosPainel);
          setOportunidades(minhasOportunidades);
        }
      } catch (error) {
        if (ativo) setErro(error.message);
      } finally {
        if (ativo) setCarregando(false);
      }
    }

    carregarPainel();
    return () => {
      ativo = false;
    };
  }, []);

  if (carregando) {
    return <main className="painel-ong"><p className="painel-ong-aviso" role="status">Carregando painel...</p></main>;
  }

  if (erro) {
    return <main className="painel-ong"><p className="painel-ong-erro" role="alert">{erro}</p></main>;
  }

  return (
    <main className="painel-ong">
      <section className="painel-ong-boas-vindas">
        <div>
          <p className="painel-ong-sobretitulo">Painel da ONG</p>
          <h1>Olá, {resumo.nomeOng}!</h1>
          <p>Acompanhe suas oportunidades e o impacto da sua organização.</p>
        </div>
        <Link to="/painel/oportunidades/nova" className="painel-ong-criar">
          + Criar oportunidade
        </Link>
      </section>

      <section className="painel-ong-indicadores" aria-label="Indicadores da ONG">
        <article className="painel-ong-indicador">
          <span>Total de oportunidades</span>
          <strong>{resumo.totalOportunidades}</strong>
        </article>
        <article className="painel-ong-indicador">
          <span>Oportunidades ativas</span>
          <strong>{resumo.oportunidadesAtivas}</strong>
        </article>
        <article className="painel-ong-indicador">
          <span>Oportunidades encerradas</span>
          <strong>{resumo.oportunidadesEncerradas}</strong>
        </article>
        <article className="painel-ong-indicador">
          <span>Voluntários inscritos</span>
          <strong>{resumo.totalVoluntariosInscritos}</strong>
        </article>
      </section>

      <section className="painel-ong-oportunidades" aria-labelledby="painel-ong-minhas-oportunidades">
        <div className="painel-ong-secao-topo">
          <div>
            <h2 id="painel-ong-minhas-oportunidades">Minhas oportunidades</h2>
            <p>Últimas oportunidades publicadas pela sua ONG.</p>
          </div>
          <Link to="/painel/oportunidades" className="painel-ong-link">Gerenciar oportunidades</Link>
        </div>

        {oportunidades.length === 0 ? (
          <div className="painel-ong-vazio">
            <p>Você ainda não publicou nenhuma oportunidade.</p>
            <Link to="/painel/oportunidades/nova">Criar a primeira oportunidade</Link>
          </div>
        ) : (
          <ul className="painel-ong-lista">
            {oportunidades.map((oportunidade) => (
              <li key={oportunidade.id} className="painel-ong-item">
                <div>
                  <h3>{oportunidade.titulo}</h3>
                  <p>{oportunidade.nomeCategoria} · {formatarData(oportunidade.data)}</p>
                </div>
                <span className={`painel-ong-status status-${oportunidade.status}`}>
                  {ROTULO_STATUS[oportunidade.status] ?? oportunidade.status}
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="painel-ong-atalhos" aria-label="Atalhos do painel">
        <Link to="/PerfilOng">Editar perfil da ONG</Link>
        <Link to="/painel/oportunidades">Administrar oportunidades</Link>
      </section>
    </main>
  );
}

export default PainelOng;
