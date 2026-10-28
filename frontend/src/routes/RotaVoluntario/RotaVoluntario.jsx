import { Navigate } from 'react-router-dom';
import { isVoluntario } from '../../services/auth.js';

// Só deixa passar quem está logado como voluntário; os demais vão para o login.
function RotaVoluntario({ children }) {
  return isVoluntario() ? children : <Navigate to="/entrar" replace />;
}

export default RotaVoluntario;
