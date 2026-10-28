import { Routes, Route } from 'react-router-dom';
import Home from '../pages/Home/Home.jsx';
import Oportunidades from '../pages/Oportunidade/Oportunidade.jsx';
import SobreNos from '../pages/Sobre/Sobre.jsx';
import ComoFunciona from '../pages/ComoFunciona/ComoFunciona.jsx';
import Contato from '../pages/Contato/Contato.jsx';
import RotaONG from './RotaONG/RotaONG.jsx';
import GerenciarOportunidades from '../pages/GerenciarOportunidades/GerenciarOportunidades.jsx';
import FormOportunidade from '../pages/FormOportunidade/FormOportunidade.jsx';
import VoluntariosInscritos from '../pages/VoluntariosInscritos/VoluntariosInscritos.jsx';
import CriarConta from '../pages/CriarConta/CriarConta.jsx';
import RotaVoluntario from './RotaVoluntario/RotaVoluntario.jsx';
import MinhasCandidaturas from '../pages/MinhasCandidaturas/MinhasCandidaturas.jsx';
import PerfilOng from "../pages/PerfilOng/PerfilOng.jsx";
import PerfilVoluntario from "../pages/PerfilVoluntario/PerfilVoluntario.jsx";
import PainelOng from "../pages/PainelOng/PainelOng.jsx";
import Login from '../pages/Login/Login.jsx';
import NaoEncontrada from '../pages/NaoEncontrada/NaoEncontrada.jsx';

function AppRoutes() {
  return (
    <Routes>
    <Route path="/login" element={<Login />} />
    <Route path="/entrar" element={<Login />} />

    <Route path="/" element={<Home />} />
    <Route path="/Home" element={<Home />} />
    <Route path="/Oportunidades" element={<Oportunidades />} />
    <Route path="/ComoFunciona" element={<ComoFunciona />} />
    <Route path="/Sobre" element={<SobreNos />} />
    <Route path="/Contato" element={<Contato />} />
    <Route path="/CriarConta" element={<CriarConta />} />

    <Route path="/PerfilOng" element={<RotaONG><PerfilOng /></RotaONG>} />
    <Route path="/PerfilVoluntario" element={<RotaVoluntario><PerfilVoluntario /></RotaVoluntario>} />

    <Route path="/painel/candidaturas" element={<RotaVoluntario><MinhasCandidaturas /></RotaVoluntario>} />

    <Route path="/painel" element={<RotaONG><PainelOng /></RotaONG>} />

    <Route path="/painel/oportunidades" element={<RotaONG><GerenciarOportunidades /></RotaONG>} />
    <Route path="/painel/oportunidades/nova" element={<RotaONG><FormOportunidade /></RotaONG>} />
    <Route path="/painel/oportunidades/:id/editar" element={<RotaONG><FormOportunidade /></RotaONG>} />

    <Route
      path="/painel/oportunidades/:id/inscritos"
      element={
        <RotaONG>
          <VoluntariosInscritos />
        </RotaONG>
      }
    />
    <Route path="*" element={<NaoEncontrada />} />
    </Routes>
  );
}

export default AppRoutes;
