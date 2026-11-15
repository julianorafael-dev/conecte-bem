import { NavLink } from 'react-router-dom';

export default function MenuOng() {
  return (
    <nav className="menu-conta" aria-label="Área da ONG">
      <span className="menu-conta-titulo">Área da ONG</span>
      <div className="menu-conta-links menu-conta-links--ong">
        <NavLink to="/painel" end>Painel da ONG</NavLink>
        <NavLink to="/painel/oportunidades">Minhas oportunidades</NavLink>
        <NavLink to="/PerfilOng">Perfil da ONG</NavLink>
      </div>
    </nav>
  );
}
