import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Header from './componentes/Header/Header.jsx';
import Home from './pages/Home/Home.jsx';
import Footer from './componentes/Footer/Footer.jsx';
import Oportunidades from './pages/Oportunidade/Oportunidade.jsx';
import SobreNos from './pages/Sobre/Sobre.jsx';
import ComoFunciona from './pages/ComoFunciona/ComoFunciona.jsx';
import Contato from './pages/Contato/Contato.jsx';

import RotaONG from './routes/RotaONG/RotaONG.jsx';
import GerenciarOportunidades from './pages/GerenciarOportunidades/GerenciarOportunidades.jsx';
import FormOportunidade from './pages/FormOportunidade/FormOportunidade.jsx';
import VoluntariosInscritos from './pages/VoluntariosInscritos/VoluntariosInscritos.jsx';
import CriarConta from './pages/CriarConta/CriarConta.jsx';
import RotaVoluntario from './routes/RotaVoluntario/RotaVoluntario.jsx';
import MinhasCandidaturas from './pages/MinhasCandidaturas/MinhasCandidaturas.jsx';
import PerfilOng from "./pages/PerfilOng/PerfilOng.jsx";
import PerfilVoluntario from "./pages/PerfilVoluntario/PerfilVoluntario.jsx";
import PainelOng from "./pages/PainelOng/PainelOng.jsx";

import { AuthProvider } from './context/AuthContext';
import Login from './pages/Login/Login.jsx';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div>
          <Header />

          <Routes>
            {/* Sua rota nova */}
            <Route path="/login" element={<Login />} />
            <Route path="/entrar" element={<Login />} />

            {/* Tudo que já existia continua igual */}
            <Route path="/" element={<Home />} />
            <Route path="/Home" element={<Home />} />
            <Route path="/Oportunidades" element={<Oportunidades />} />
            <Route path="/ComoFunciona" element={<ComoFunciona />} />
            <Route path="/Sobre" element={<SobreNos />} />
            <Route path="/Contato" element={<Contato />} />
            <Route path="/CriarConta" element={<CriarConta />} />

            {/* Rotas de perfil ainda abertas temporariamente para testes. */}
            <Route path="/PerfilOng" element={<PerfilOng />} />
            <Route path="/PerfilVoluntario" element={<PerfilVoluntario />} />

            {/* Área do voluntário (só acessa quem estiver logado como voluntário) */}
            <Route path="/painel/candidaturas" element={<RotaVoluntario><MinhasCandidaturas /></RotaVoluntario>}/>

            {/* Área da ONG (só acessa quem estiver logado como ONG) */}
            <Route path="/painel" element={<RotaONG><PainelOng /></RotaONG>} />

            <Route path="/painel/oportunidades" element={<RotaONG><GerenciarOportunidades/></RotaONG>}/>
            <Route path="/painel/oportunidades/nova" element={<RotaONG><FormOportunidade /></RotaONG>}/>
            <Route path="/painel/oportunidades/:id/editar" element={<RotaONG><FormOportunidade /></RotaONG>}/>
            {/* <Route path="/painel/perfil" element={<RotaONG><PerfilOng /></RotaONG>}/> */}

            <Route
              path="/painel/oportunidades/:id/inscritos"
              element={
                <RotaONG>
                  <VoluntariosInscritos />
                </RotaONG>
              }
            />
          </Routes>

          <Footer />
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
