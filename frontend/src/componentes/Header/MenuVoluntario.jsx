import { Link, useLocation } from 'react-router-dom';

const itens = [
  { titulo: 'Meu cadastro', destino: '/MeuCadastro', caminhos: ['/meucadastro', '/perfilvoluntario'] },
  { titulo: 'Minhas inscrições', destino: '/painel/candidaturas', caminhos: ['/painel/candidaturas'] },
];

export default function MenuVoluntario() {
  const { pathname } = useLocation();
  const caminho = pathname.toLowerCase().replace(/\/+$/, '');

  return (
    <nav className="menu-conta" aria-label="Área do voluntário">
      <span className="menu-conta-titulo">Área do voluntário</span>
      <div className="menu-conta-links">
        {itens.map(({ titulo, destino, caminhos }) => (
          <Link key={destino} to={destino} aria-current={caminhos.includes(caminho) ? 'page' : undefined}>
            {titulo}
          </Link>
        ))}
      </div>
    </nav>
  );
}
