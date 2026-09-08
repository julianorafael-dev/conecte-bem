import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './routes/PrivateRoute';
import Login from 'pages/login';
import Cadastro from './pages/cadrasto';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/cadastro" element={<Cadastro />} />
          <Route
            path="/protegida"
            element={
              <PrivateRoute>
                <div style={{ padding: '2rem' }}>
                  <h2>Bem-vindo! Você está logado ✅</h2>
                  <p>Essa página só aparece para usuários logados.</p>
                </div>
              </PrivateRoute>
            }
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;