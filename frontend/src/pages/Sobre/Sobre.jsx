import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBullseye, faEye, faHandHoldingHeart } from '@fortawesome/free-solid-svg-icons';
import fotoSobre from '../../assets/foto-sobre.avif';
import './Sobre.css';

const PILARES = [
  {
    id: 1,
    titulo: 'Missão',
    icone: faBullseye,
    texto: 'Aproximar quem quer ajudar de quem precisa de ajuda, tornando o voluntariado simples, acessível e seguro para todos.'
  },
  {
    id: 2,
    titulo: 'Visão',
    icone: faEye,
    texto: 'Ser uma referência em voluntariado, uma comunidade em que doar tempo e talento faça parte da rotina das pessoas.'
  },
  {
    id: 3,
    titulo: 'Valores',
    icone: faHandHoldingHeart,
    texto: 'Solidariedade, transparência, respeito e compromisso com o impacto real na vida das pessoas e das comunidades.'
  }
];

function SobreNos() {
  return (
    <main className="sobre">
      <section className="sobre-topo">
        <h1>Sobre Nós</h1>
        <p>
          O Conecte-Bem é uma iniciativa privada e sem fins lucrativos que conecta voluntários a
          ONGs, pessoas e instituições que precisam de ajuda. É por meio do trabalho voluntário que
          geramos impacto positivo na vida de quem mais precisa.
        </p>
      </section>

      <div className="sobre-imagem">
        <img
          src={fotoSobre}
          alt="Várias pessoas com as mãos juntas"
          loading="lazy"
        />
      </div>

      <section className="sobre-pilares">
        {PILARES.map((pilar) => (
          <article key={pilar.id} className="sobre-card">
            <span className="sobre-icone">
              <FontAwesomeIcon icon={pilar.icone} />
            </span>
            <h2>{pilar.titulo}</h2>
            <p>{pilar.texto}</p>
          </article>
        ))}
      </section>

      <section className="sobre-como">
        <h2>Como atuamos</h2>
        <p>
          As ONGs publicam oportunidades de voluntariado na plataforma, e os voluntários escolhem as
          causas com que mais se identificam e se inscrevem. As organizações acompanham quem se
          candidatou e entram em contato diretamente, sem burocracia.
        </p>
      </section>

      <section className="sobre-chamada">
        <h2>Faça parte dessa rede</h2>
        <p>Seja voluntário ou represente uma ONG, há um lugar para você no Conecte-Bem.</p>
        <div className="sobre-botoes">
          <Link to="/Oportunidades" className="sobre-botao sobre-botao-principal">Ver oportunidades</Link>
          <Link to="/CriarConta" className="sobre-botao">Criar conta</Link>
        </div>
      </section>
    </main>
  );
}

export default SobreNos;
