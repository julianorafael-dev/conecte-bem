import { getToken, removeToken, EVENTO_SESSAO_EXPIRADA } from './auth.js';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Wrapper simples do fetch: coloca o token JWT e devolve a mensagem de erro do backend
// (o GlobalExceptionHandler responde { timestamp, status, erro, mensagem }).
export async function api(caminho, { method = 'GET', body } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  // Rotas /auth/* (login, registro) nunca levam token: um token velho no navegador
  // não pode transformar "senha incorreta" em "sessão expirada".
  const token = caminho.startsWith('/auth/') ? null : getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  let resposta;
  try {
    resposta = await fetch(`${API_URL}${caminho}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new Error('Não foi possível conectar ao servidor.');
  }

  if (resposta.status === 204) return null;

  const dados = await resposta.json().catch(() => null);

  if (!resposta.ok) {
    // 401 com token enviado = token expirado/inválido: encerra a sessão.
    if (resposta.status === 401 && token) {
      removeToken();
      window.dispatchEvent(new Event(EVENTO_SESSAO_EXPIRADA));
      throw new Error('Sua sessão expirou. Entre novamente.');
    }
    if (resposta.status === 403) {
      throw new Error(dados?.mensagem || 'Você não tem permissão para realizar esta ação.');
    }
    throw new Error(dados?.mensagem || 'Erro inesperado. Tente novamente.');
  }
  return dados;
}
