import Header from './componentes/Header/Header.jsx'
import Footer from './componentes/Footer/Footer.jsx'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

function App() {
  

  return (
    <>
    <Header />
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/cadastro" element={<Cadastro />} />
        <Route path="/perfil" element={<Perfil />} />
      </Routes>
    </BrowserRouter>
    <Footer />
    </>
  )
}

export default App
