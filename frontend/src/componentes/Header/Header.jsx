import './Header.css'


function Header() {
  return (
    <header className="header">

    <a href="/" className="logo">
        <span className="coracao">&#9825;</span>
        <strong>Conecte-Bem</strong>
    </a>

    <nav className="menu">
        <a href="/">Início</a>
        <a href="/oportunidades">Oportunidades</a>
        <a href="/como-funciona">Como Funciona</a>
        <a href="/sobre">Sobre nós</a>
    </nav>

    <div className="menu_login_cadastro">
        <a href="/entrar">Entrar</a>
        <a href="/criar-conta">Criar Conta</a>
    </div>

</header>
  )
}

export default Header