import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import { api } from '../../services/api.js';
import './Header.css';
import MenuVoluntario from './MenuVoluntario.jsx';
import MenuOng from './MenuOng.jsx';

function Header() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const [perfil, setPerfil] = useState(null);
  const nome = perfil?.usuario === usuario ? perfil.nome : '';

  useEffect(() => {
    const endpoint = usuario?.tipo === 'ONG' ? '/ongs/me'
      : usuario?.tipo === 'VOLUNTARIO' ? '/voluntarios/me' : null;
    if (!endpoint) return undefined;
    let ativo = true;
    api(endpoint)
      .then(dados => {
        if (ativo) setPerfil({ usuario, nome: dados?.nome?.trim() || '' });
      })
      .catch(() => {
        if (ativo) setPerfil({ usuario, nome: '' });
      });
    return () => { ativo = false; };
  }, [usuario, pathname]);

  useEffect(() => {
    const tipo = usuario?.tipo;
    document.title = tipo === 'VOLUNTARIO'
      ? 'Conecte-Bem | Voluntário'
      : tipo === 'ONG'
        ? 'Conecte-Bem | ONG'
        : 'Conecte-Bem';
  }, [usuario?.tipo]);

  function sair() {
    logout();
    navigate('/Home');
  }

  return (
    <>
    <header className={`header${usuario ? ' header--autenticado' : ''}`}>
      <Link to="/Home" className="logo">
        <span className="coracao">&#9825;</span>
        <strong>Conecte-Bem</strong>
      </Link>

      <nav className="menu" aria-label="Navegação principal">
        <Link to="/Home">Início</Link>
        <Link to="/Oportunidades">Oportunidades</Link>
        <Link to="/ComoFunciona">Como Funciona</Link>
        <Link to="/Sobre">Sobre nós</Link>
      </nav>

      <div className="menu_login_cadastro">
        {usuario ? (
          <>
            <span className="header-saudacao" title={nome || undefined}>
              Olá{nome ? <>, <strong>{nome}</strong></> : '!'}
            </span>
            <button type="button" className="btn-sair" onClick={sair}>Sair</button>
          </>
        ) : (
          <>
            <Link to="/entrar">Entrar</Link>
            <Link to="/CriarConta">Criar Conta</Link>
          </>
        )}
      </div>
    </header>
    {usuario?.tipo === 'VOLUNTARIO' && <MenuVoluntario />}
    {usuario?.tipo === 'ONG' && <MenuOng />}
    </>
  );
}

export default Header;
