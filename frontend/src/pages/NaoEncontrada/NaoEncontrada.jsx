import { Link } from 'react-router-dom';
import './NaoEncontrada.css';

function NaoEncontrada() {
  return (
    <main className="nao-encontrada">
      <p className="nao-encontrada-codigo">404</p>
      <h1>Página não encontrada</h1>
      <p>O endereço que você acessou não existe. Volte ao início para continuar.</p>
      <Link to="/">Voltar ao início</Link>
    </main>
  );
}

export default NaoEncontrada;
