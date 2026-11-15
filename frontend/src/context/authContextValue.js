import { createContext } from 'react';

// Objeto de contexto separado do Provider para o lint (react-refresh) aceitar
// que AuthContext.jsx exporte somente o componente.
export const AuthContext = createContext(null);
