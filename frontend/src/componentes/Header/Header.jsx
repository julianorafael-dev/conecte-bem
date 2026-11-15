import { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import { api } from '../../services/api.js';
import './Header.css';
import MenuVoluntario from './MenuVoluntario.jsx';
import MenuOng from './MenuOng.jsx';

function Header() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const { pathname, key: locationKey } = useLocation();
  const [menuLocation, setMenuLocation] = useState(null);
  const menuAberto = menuLocation === locationKey;
  const botaoMenu = useRef(null);
  const [perfil, setPerfil] = useState(null);
  const nome = perfil?.usuario === usuario ? perfil.nome : '';

  useEffect(() => {
    function fecharComEscape(event) {
      if (event.key === 'Escape' && menuAberto) {
        setMenuLocation(null);
        botaoMenu.current?.focus();
      }
    }
    const desktop = window.matchMedia('(min-width: 1025px)');
    function fecharNoDesktop(event) {
      if (event.matches) setMenuLocation(null);
    }
    document.addEventListener('keydown', fecharComEscape);
    desktop.addEventListener('change', fecharNoDesktop);
    return () => {
      document.removeEventListener('keydown', fecharComEscape);
      desktop.removeEventListener('change', fecharNoDesktop);
    };
  }, [menuAberto]);

  function fecharAoNavegar(event) {
    if (event.target.closest('a, .btn-sair')) setMenuLocation(null);
  }

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
      <div className={`site-header${menuAberto ? ' site-header--aberto' : ''}`} onClick={fecharAoNavegar}>
        <header className={`header${usuario ? ' header--autenticado' : ''}`}>
          <Link to="/Home" className="logo">
            <span className="coracao">&#9825;</span>
            <strong>Conecte-Bem</strong>
          </Link>

          <button
            ref={botaoMenu}
            type="button"
            className="menu-toggle"
            aria-label={menuAberto ? 'Fechar menu' : 'Abrir menu'}
            aria-expanded={menuAberto}
            aria-controls={usuario ? 'header-navigation' : 'header-navigation header-session'}
            onClick={() => setMenuLocation(menuAberto ? null : locationKey)}
          >
            <span aria-hidden="true">{menuAberto ? '✕' : '☰'}</span>
          </button>

          <div id="header-navigation" className="header-navigation">
          <nav className="menu" aria-label="Navegação principal">
            <Link to="/Home">Início</Link>
            <Link to="/Oportunidades">Oportunidades</Link>
            <Link to="/ComoFunciona">Como Funciona</Link>
            <Link to="/Sobre">Sobre nós</Link>
          </nav>
          </div>

          <div id="header-session" className="menu_login_cadastro">
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
        <div id="header-account" className="header-account">
        {usuario?.tipo === 'VOLUNTARIO' && <MenuVoluntario />}
        {usuario?.tipo === 'ONG' && <MenuOng />}
        </div>
      </div>
  );
}

export default Header;
