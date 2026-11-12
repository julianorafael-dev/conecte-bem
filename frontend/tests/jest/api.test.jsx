import { api } from '../../src/services/api.js';
import { EVENTO_SESSAO_EXPIRADA, getToken, setToken } from '../../src/services/auth.js';
import { respostaFetch } from './utils.jsx';

describe('services/api.js', () => {
  beforeEach(() => {
    localStorage.clear();
    global.fetch = jest.fn();
  });

  it('faz GET na URL padrão da API e devolve o JSON', async () => {
    fetch.mockReturnValue(respostaFetch(200, [{ id: 1 }]));
    await expect(api('/categorias')).resolves.toEqual([{ id: 1 }]);
    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/categorias', expect.objectContaining({ method: 'GET' }));
  });

  it('envia o corpo em JSON com Content-Type', async () => {
    fetch.mockReturnValue(respostaFetch(201, { id: 9 }));
    await api('/oportunidades', { method: 'POST', body: { titulo: 'X' } });
    const [, opcoes] = fetch.mock.calls[0];
    expect(opcoes.headers['Content-Type']).toBe('application/json');
    expect(opcoes.body).toBe(JSON.stringify({ titulo: 'X' }));
  });

  it('envia o token no cabeçalho Authorization', async () => {
    setToken('meu-token');
    fetch.mockReturnValue(respostaFetch(200, {}));
    await api('/inscricoes/me');
    expect(fetch.mock.calls[0][1].headers.Authorization).toBe('Bearer meu-token');
  });

  it('não envia token nas rotas /auth/*', async () => {
    setToken('token-velho');
    fetch.mockReturnValue(respostaFetch(200, { token: 'novo' }));
    await api('/auth/login', { method: 'POST', body: {} });
    expect(fetch.mock.calls[0][1].headers.Authorization).toBeUndefined();
  });

  it('devolve null em respostas 204', async () => {
    fetch.mockReturnValue(respostaFetch(204));
    await expect(api('/inscricoes/1', { method: 'DELETE' })).resolves.toBeNull();
  });

  it('mostra mensagem amigável quando o servidor está fora do ar', async () => {
    fetch.mockRejectedValue(new TypeError('Failed to fetch'));
    await expect(api('/oportunidades')).rejects.toThrow('Não foi possível conectar ao servidor.');
  });

  it('em 401 com token, encerra a sessão e avisa a aplicação', async () => {
    setToken('expirado');
    const ouvinte = jest.fn();
    window.addEventListener(EVENTO_SESSAO_EXPIRADA, ouvinte);
    fetch.mockReturnValue(respostaFetch(401, undefined));
    await expect(api('/inscricoes/me')).rejects.toThrow('Sua sessão expirou. Entre novamente.');
    expect(getToken()).toBeNull();
    expect(ouvinte).toHaveBeenCalledTimes(1);
    window.removeEventListener(EVENTO_SESSAO_EXPIRADA, ouvinte);
  });

  it('em 401 sem token (login errado), mostra a mensagem do backend', async () => {
    fetch.mockReturnValue(respostaFetch(401, { mensagem: 'E-mail ou senha incorretos.' }));
    await expect(api('/auth/login', { method: 'POST', body: {} })).rejects.toThrow('E-mail ou senha incorretos.');
  });

  it('em 403 usa a mensagem do backend ou uma mensagem padrão', async () => {
    fetch.mockReturnValue(respostaFetch(403, { mensagem: 'Você só pode alterar oportunidades da sua própria ONG.' }));
    await expect(api('/oportunidades/1', { method: 'PUT', body: {} })).rejects.toThrow('sua própria ONG');
    fetch.mockReturnValue(respostaFetch(403, undefined));
    await expect(api('/x')).rejects.toThrow('Você não tem permissão para realizar esta ação.');
  });

  it('em outros erros usa a mensagem do backend ou uma mensagem padrão', async () => {
    fetch.mockReturnValue(respostaFetch(409, { mensagem: 'Você já possui uma inscrição ativa nesta oportunidade.' }));
    await expect(api('/inscricoes', { method: 'POST', body: {} })).rejects.toThrow('inscrição ativa');
    fetch.mockReturnValue(respostaFetch(500, undefined));
    await expect(api('/x')).rejects.toThrow('Erro inesperado. Tente novamente.');
  });
});
