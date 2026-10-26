import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';


import Header from './componentes/Header/Header.jsx';
import Home from './componentes/Home/Home.jsx';
import Footer from './componentes/Footer/Footer.jsx';
import Oportunidades from './componentes/Oportunidade/Oportunidade.jsx';
import SobreNos from './componentes/Sobre/Sobre.jsx';
import ComoFunciona from './componentes/ComoFunciona/ComoFunciona.jsx';
import Contato from './componentes/Contato/Contato.jsx';

import RotaONG from './componentes/RotaONG/RotaONG.jsx';
import GerenciarOportunidades from './componentes/GerenciarOportunidades/GerenciarOportunidades.jsx';
import FormOportunidade from './componentes/FormOportunidade/FormOportunidade.jsx';
import VoluntariosInscritos from './componentes/VoluntariosInscritos/VoluntariosInscritos.jsx';
import CriarConta from './componentes/CriarConta/CriarConta.jsx';
import RotaVoluntario from './componentes/RotaVoluntario/RotaVoluntario.jsx';
import MinhasCandidaturas from './componentes/MinhasCandidaturas/MinhasCandidaturas.jsx';
import PerfilOng from "./pages/PerfilOng/PerfilOng.jsx";
import PerfilVoluntario from "./pages/PerfilVoluntario/PerfilVoluntario.jsx";
import PainelOng from "./pages/PainelOng/PainelOng.jsx";


function App() {
  return (
    <Router>
      <div>
        <Header />

        <Routes>
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
  );
}

export default App
