import { getToken } from './auth.js';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Wrapper simples do fetch: coloca o token JWT e devolve a mensagem de erro do backend
// (o GlobalExceptionHandler responde { timestamp, status, erro, mensagem }).
export async function api(caminho, { method = 'GET', body } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const token = getToken();
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
    if (resposta.status === 401 || resposta.status === 403) {
      throw new Error('Sessão expirada ou sem permissão. Entre novamente.');
    }
    throw new Error(dados?.mensagem || 'Erro inesperado. Tente novamente.');
  }
  return dados;
}
