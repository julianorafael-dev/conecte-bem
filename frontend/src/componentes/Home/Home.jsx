import { useState } from 'react'; // 1. Importe o useState
import '../../styles/global.css';
import '../../componentes/Home/Home.css';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBookOpen, faHandHoldingMedical, faLeaf, faPeopleArrows, faPeopleGroup, faPeopleRoof, faTimes 
} from '@fortawesome/free-solid-svg-icons';

// 2. Dados dos cards em uma lista para facilitar a exibição e o Modal
const AREAS_VOLUNTARIADO = [
  {
    id: 1,
    titulo: 'Educação e Cultura',
    icone: faBookOpen,
    descricao: 'Apoie projetos educacionais, aulas de reforço, contação de histórias e oficinas culturais para crianças e adultos.'
  },
  {
    id: 2,
    titulo: 'Saúde e Apoio Emocional',
    icone: faHandHoldingMedical,
    descricao: 'Participe de ações de apoio a pacientes, campanhas de doação de sangue, escuta empática e suporte em hospitais.'
  },
  {
    id: 3,
    titulo: 'Meio Ambiente e Animais',
    icone: faLeaf,
    descricao: 'Ajude na preservação ambiental, mutirões de reciclagem, proteção animal e cuidados em abrigos.'
  },
  {
    id: 4,
    titulo: 'Assistência Social e Comunitária',
    icone: faPeopleGroup,
    descricao: 'Contribua com a distribuição de alimentos, agasalhos e desenvolvimento social em comunidades vulneráveis.'
  },
  {
    id: 5,
    titulo: 'Voluntariado Digital',
    icone: faPeopleArrows,
    descricao: 'Doando suas habilidades remotamente em design, programação, redes sociais e gestão de projetos para ONGs.'
  },
  {
    id: 6,
    titulo: 'Turismo Social',
    icone: faPeopleRoof,
    descricao: 'Participe de viagens e expedições voltadas ao desenvolvimento sustentável e apoio comunitário local.'
  }
];

function Home() {
  // Estado para controlar qual card está aberto no Modal
  const [cardSelecionado, setCardSelecionado] = useState(null);

  // Função para fechar o Modal
  const fecharModal = () => setCardSelecionado(null);

  return (
    <div className="home-container">
      <main className="conteudo_principal">
        <section className="titulo_principal">
          <h1>Conectando voluntários a ONGs</h1>
          <p className="texto_secundario">Transformando realidades através do voluntariado.</p>
        </section>

        <section className="container_cards">
          <h2>Conheça as áreas do Voluntariado</h2>
          <p className="texto_secundario">Busque causas que combinem com você.</p>
          
          <div className="conteudo_card">
            {AREAS_VOLUNTARIADO.map((item) => (
              <div 
                key={item.id} 
                className="card" 
                onClick={() => setCardSelecionado(item)} // Ao clicar, define o card ativo
              >
                <div className="card_titulo">
                  <span>{item.titulo}</span>
                </div>
                <FontAwesomeIcon icon={item.icone} />
              </div>
            ))}
          </div>
        </section>
      </main>

      {/* 3. ESTRUTURA DO MODAL */}
      {cardSelecionado && (
        <div className="modal-overlay" onClick={fecharModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={fecharModal}>
              <FontAwesomeIcon icon={faTimes} />
            </button>
            <div className="modal-header">
              <FontAwesomeIcon icon={cardSelecionado.icone} className="modal-icon" />
              <h2>{cardSelecionado.titulo}</h2>
            </div>
            <div className="modal-body">
              <p>{cardSelecionado.descricao}</p>
            </div>
            {/*<div className="modal-footer">
              <button className="btn-participar">Quero participar</button>
            </div> teria que conectar esse botão...*/}
          </div>
        </div>
      )}
    </div>
  );
}

export default Home;