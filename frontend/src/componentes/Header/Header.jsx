import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import './Header.css';

function Header() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();

  function sair() {
    logout();
    navigate('/Home');
  }

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
        {usuario?.tipo === 'ONG' && <Link to="/painel">Painel da ONG</Link>}
      </nav>

      <div className="menu_login_cadastro">
        {usuario ? (
          <button type="button" className="btn-sair" onClick={sair}>Sair</button>
        ) : (
          <>
            <Link to="/entrar">Entrar</Link>
            <Link to="/CriarConta">Criar Conta</Link>
          </>
        )}
      </div>
    </header>
  );
}

export default Header;
