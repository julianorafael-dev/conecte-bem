import { useCallback, useEffect, useMemo, useState } from 'react';
import { api } from '../services/api.js';
import {
  CHAVE_TOKEN,
  EVENTO_SESSAO_EXPIRADA,
  getToken,
  getUsuario,
  removeToken,
  setToken,
} from '../services/auth.js';
import { AuthContext } from './authContextValue.js';

export function AuthProvider({ children }) {
  // Restaura a sessão ao recarregar a página a partir do JWT salvo.
  const [usuario, setUsuario] = useState(() => {
    const atual = getUsuario();
    if (!atual && getToken()) removeToken(); // limpa token expirado/corrompido
    return atual;
  });

  const logout = useCallback(() => {
    removeToken();
    setUsuario(null);
  }, []);

  // Autentica no backend, guarda o JWT e devolve { id, email, tipo }.
  const login = useCallback(async (email, senha) => {
    const { token } = await api('/auth/login', { method: 'POST', body: { email, senha } });
    setToken(token);
    const autenticado = getUsuario();
    setUsuario(autenticado);
    return autenticado;
  }, []);

  // Cria a conta (voluntário ou ONG) sem iniciar sessão.
  const registrar = useCallback(
    (dados) => api('/auth/registro', { method: 'POST', body: dados }),
    []
  );

  // O api.js avisa quando o backend recusa o token (401); outras abas avisam via "storage".
  useEffect(() => {
    const aoExpirar = () => setUsuario(null);
    const aoMudarStorage = (evento) => {
      if (evento.key === CHAVE_TOKEN || evento.key === null) setUsuario(getUsuario());
    };
    window.addEventListener(EVENTO_SESSAO_EXPIRADA, aoExpirar);
    window.addEventListener('storage', aoMudarStorage);
    return () => {
      window.removeEventListener(EVENTO_SESSAO_EXPIRADA, aoExpirar);
      window.removeEventListener('storage', aoMudarStorage);
    };
  }, []);

  // Encerra a sessão sozinha quando o token vence.
  useEffect(() => {
    if (!usuario?.exp) return undefined;
    const restante = usuario.exp * 1000 - Date.now();
    const temporizador = setTimeout(logout, Math.max(restante, 0));
    return () => clearTimeout(temporizador);
  }, [usuario, logout]);

  const valor = useMemo(
    () => ({ usuario, login, logout, registrar }),
    [usuario, login, logout, registrar]
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}
