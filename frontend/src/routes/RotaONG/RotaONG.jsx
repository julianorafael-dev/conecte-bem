import { Navigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';

// Só deixa passar quem está logado como ONG; os demais vão para o login.
function RotaONG({ children }) {
  const { usuario } = useAuth();
  return usuario?.tipo === 'ONG' ? children : <Navigate to="/entrar" replace />;
}

export default RotaONG;
