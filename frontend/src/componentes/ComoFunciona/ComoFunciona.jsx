import { Link } from 'react-router-dom';
import fotoComoFunciona from '../../midia/foto-comofunc.avif';
import './ComoFunciona.css';

const PASSOS_VOLUNTARIO = [
  {
    titulo: 'Crie sua conta',
    texto: 'Cadastre-se gratuitamente com seus dados básicos.'
  },
  {
    titulo: 'Encontre uma oportunidade',
    texto: 'Explore as ações disponíveis e escolha a causa, a data e a cidade que combinam com você.'
  },
  {
    titulo: 'Inscreva-se',
    texto: 'Candidate-se à vaga com poucos cliques. A ONG recebe o seu contato.'
  },
  {
    titulo: 'Colabore',
    texto: 'Compareça no dia combinado e ajude a transformar a realidade de quem precisa.'
  }
];

const PASSOS_ONG = [
  {
    titulo: 'Cadastre sua ONG',
    texto: 'Crie a conta da organização e apresente o seu trabalho.'
  },
  {
    titulo: 'Publique oportunidades',
    texto: 'Informe a data, o horário, o local e o número de vagas de cada ação.'
  },
  {
    titulo: 'Acompanhe os inscritos',
    texto: 'Veja quem se candidatou a cada vaga, com nome e e-mail de contato.'
  },
  {
    titulo: 'Conecte-se com os voluntários',
    texto: 'Entre em contato com os inscritos e organize a participação de cada um.'
  }
];

function ListaPassos({ passos }) {
  return (
    <ol className="como-passos">
      {passos.map((passo) => (
        <li key={passo.titulo}>
          <h3>{passo.titulo}</h3>
          <p>{passo.texto}</p>
        </li>
      ))}
    </ol>
  );
}

function ComoFunciona() {
  return (
    <main className="como">
      <section className="como-topo">
        <h1>Como Funciona</h1>
        <p>
          Conectar voluntários e ONGs é simples. O voluntariado é a doação espontânea de tempo,
          trabalho e habilidades para apoiar causas sociais, sem remuneração. Veja como participar.
        </p>
      </section>

      <div className="como-imagem">
        <img
          src={fotoComoFunciona}
          alt="Voluntário sorrindo ao entregar um pacote de alimento a um senhor, em uma ação de doação de alimentos."
          loading="lazy"
        />
      </div>

      <section className="como-colunas">
        <article className="como-bloco">
          <h2>Para voluntários</h2>
          <ListaPassos passos={PASSOS_VOLUNTARIO} />
        </article>

        <article className="como-bloco">
          <h2>Para ONGs</h2>
          <ListaPassos passos={PASSOS_ONG} />
        </article>
      </section>

      <section className="como-chamada">
        <h2>Pronto para começar?</h2>
        <p>Crie sua conta e faça parte de uma rede que transforma realidades.</p>
        <div className="como-botoes">
          <Link to="/CriarConta" className="como-botao como-botao-principal">Criar conta</Link>
          <Link to="/Oportunidades" className="como-botao">Ver oportunidades</Link>
        </div>
      </section>
    </main>
  );
}

export default ComoFunciona;
