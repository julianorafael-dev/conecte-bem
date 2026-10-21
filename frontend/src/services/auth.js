// ATENÇÃO: o nome da chave abaixo precisa ser o MESMO que a tela de Login
// usa para salvar o token. Se o login salvar com outro nome, troque aqui.
const CHAVE_TOKEN = 'token';

export function getToken() {
  return localStorage.getItem(CHAVE_TOKEN);
}

// O JWT do backend carrega: sub (e-mail), id (id do usuário) e tipo (ONG/VOLUNTARIO).
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
    return { id: dados.id, email: dados.sub, tipo: dados.tipo };
  } catch {
    return null;
  }
}

export function isOng() {
  return getUsuario()?.tipo === 'ONG';
}
