import { useContext, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

function PrivateRoute({ children }) {
  const { usuario, verificarLogin } = useContext(AuthContext);

  useEffect(() => {
    verificarLogin();
  }, []);

  if (!usuario) {
    return <Navigate to="/" />;
  }

  return children;
}

export default PrivateRoute;