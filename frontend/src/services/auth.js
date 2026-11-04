// Única fonte da sessão: o JWT salvo no localStorage.
// O contexto (AuthContext) e o api.js usam estas funções.
export const CHAVE_TOKEN = 'token';
export const EVENTO_SESSAO_EXPIRADA = 'conectebem:sessao-expirada';

export function getToken() {
  return localStorage.getItem(CHAVE_TOKEN);
}

export function setToken(token) {
  localStorage.setItem(CHAVE_TOKEN, token);
}

export function removeToken() {
  localStorage.removeItem(CHAVE_TOKEN);
}

// O JWT do backend carrega: sub (e-mail), id (id do usuário), tipo (ONG/VOLUNTARIO) e exp.
export function getUsuario() {
  const token = getToken();
  if (!token) return null;
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    const dados = JSON.parse(json);
    if (dados.exp && dados.exp * 1000 < Date.now()) return null; // token expirado
    return { id: dados.id, email: dados.sub, tipo: dados.tipo, exp: dados.exp };
  } catch {
    return null;
  }
}

export function isOng() {
  return getUsuario()?.tipo === 'ONG';
}

export function isVoluntario() {
  return getUsuario()?.tipo === 'VOLUNTARIO';
}
