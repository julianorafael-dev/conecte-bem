import { createContext, useState } from 'react';

export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);

  const login = (email, senha) => {
    // Aqui vai validar com a API
    setUsuario({ email });
    localStorage.setItem('usuario', JSON.stringify({ email }));
  };

  const logout = () => {
    setUsuario(null);
    localStorage.removeItem('usuario');
  };

  const verificarLogin = () => {
    const salvo = localStorage.getItem('usuario');
    if (salvo) setUsuario(JSON.parse(salvo));
  };

  return (
    <AuthContext.Provider value={{ usuario, login, logout, verificarLogin }}>
      {children}
    </AuthContext.Provider>
  );
}