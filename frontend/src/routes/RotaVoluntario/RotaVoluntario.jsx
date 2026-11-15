import { Navigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';

// Só deixa passar quem está logado como voluntário; os demais vão para o login.
function RotaVoluntario({ children }) {
  const { usuario } = useAuth();
  return usuario?.tipo === 'VOLUNTARIO' ? children : <Navigate to="/entrar" replace />;
}

export default RotaVoluntario;
