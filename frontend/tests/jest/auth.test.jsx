import { getToken, getUsuario, isOng, isVoluntario, removeToken, setToken } from '../../src/services/auth.js';
import { criarToken } from './utils.jsx';

describe('services/auth.js', () => {
  beforeEach(() => localStorage.clear());

  it('salva, lê e remove o token do localStorage', () => {
    setToken('abc');
    expect(getToken()).toBe('abc');
    removeToken();
    expect(getToken()).toBeNull();
  });

  it('retorna null quando não há token', () => {
    expect(getUsuario()).toBeNull();
  });

  it('extrai id, e-mail e tipo de um token válido', () => {
    setToken(criarToken({ id: 7, email: 'ong@exemplo.com', tipo: 'ONG' }));
    expect(getUsuario()).toMatchObject({ id: 7, email: 'ong@exemplo.com', tipo: 'ONG' });
  });

  it('decodifica e-mail com acentos (UTF-8)', () => {
    setToken(criarToken({ email: 'joão@exemplo.com' }));
    expect(getUsuario().email).toBe('joão@exemplo.com');
  });

  it('considera expirado um token vencido', () => {
    setToken(criarToken({ expiraEmSegundos: -60 }));
    expect(getUsuario()).toBeNull();
  });

  it('retorna null para token malformado', () => {
    setToken('isto-nao-e-um-jwt');
    expect(getUsuario()).toBeNull();
  });

  it('identifica o perfil com isOng e isVoluntario', () => {
    setToken(criarToken({ tipo: 'ONG' }));
    expect(isOng()).toBe(true);
    expect(isVoluntario()).toBe(false);
    setToken(criarToken({ tipo: 'VOLUNTARIO' }));
    expect(isOng()).toBe(false);
    expect(isVoluntario()).toBe(true);
  });
});
