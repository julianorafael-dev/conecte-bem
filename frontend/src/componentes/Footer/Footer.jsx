import { Link } from 'react-router-dom';
import './Footer.css'

function Footer() {
    return (
        <footer className="footer">
            <div className="footer_superior">
                <div className="footer_logo">
                    <a href="/" className="logo">
                        <span className="coracao">&#9825;</span>
                        <strong>Conecte-Bem</strong>
                    </a>
                    <p>Conectando voluntários a ONGs para transformar realidades através do voluntariado.</p>
                </div>
                <div className="footer_plataforma">
                    <h3>Plataforma</h3>
                    <ul>
                        <li><Link to="/Oportunidades">Oportunidades</Link></li>
                        <li><Link to="/ComoFunciona">Como funciona</Link></li>
                        <li><Link to="/">Para ONGs</Link></li>
                    </ul>
                </div>
                <div className="footer_conta">
                    <h3>Conta</h3>
                    <ul>
                        <li><Link to="/">Entrar</Link></li>
                        <li><Link to="/">Criar conta</Link></li>
                        <li><Link to="/">Recuperar senha</Link></li>
                    </ul>
                </div>
                <div className="footer_sobre">
                    <h3>Sobre</h3>
                    <ul>
                        <li><Link to="/Sobre">Nossa história</Link></li>
                        <li><Link to="/Contato">Contato</Link></li>
                        
                    </ul>
                </div>
            </div>
            <div className="footer_inferior">
                <p>&copy; 2026 Conecte-Bem -- Projeto de TCC - Bootcamp Full Stack.</p>
                <p>Feito com <span className="coracaov">❤</span> para impactar o mundo.</p>
            </div>
        </footer>
    )
}

export default Footer