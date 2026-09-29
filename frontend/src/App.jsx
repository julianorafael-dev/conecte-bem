import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';


import Header from './componentes/Header/Header.jsx';
import Home from './componentes/Home/Home.jsx';
import Footer from './componentes/Footer/Footer.jsx';
import Oportunidades from './componentes/Oportunidade/Oportunidade.jsx';
import SobreNos from './componentes/Sobre/Sobre.jsx';
import ComoFunciona from './componentes/ComoFunciona/ComoFunciona.jsx';
import Contato from './componentes/Contato/Contato.jsx';


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
          
        </Routes>

        <Footer />
      </div>
    </Router>
  );
}

export default App
