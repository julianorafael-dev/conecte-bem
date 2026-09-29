
import { Link } from 'react-router-dom';
import './Header.css';

function Header() {
  return (
    <header className="header">
      <Link to="/Home" className="logo">
        <span className="coracao">&#9825;</span>
        <strong>Conecte-Bem</strong>
      </Link>

      <nav className="menu">
        <Link to="/Home">Início</Link>
        <Link to="/Oportunidades">Oportunidades</Link>
        <Link to="/ComoFunciona">Como Funciona</Link>
        <Link to="/Sobre">Sobre nós</Link>
      </nav>

      <div className="menu_login_cadastro">
        <Link to="/entrar">Entrar</Link>
        <Link to="/criar-conta">Criar Conta</Link>
      </div>
    </header>
  );
}

export default Header;