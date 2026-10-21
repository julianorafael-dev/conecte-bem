import { Navigate } from 'react-router-dom';
import { isOng } from '../../services/auth.js';

// Só deixa passar quem está logado como ONG; os demais vão para o login.
function RotaONG({ children }) {
  return isOng() ? children : <Navigate to="/entrar" replace />;
}

export default RotaONG;
