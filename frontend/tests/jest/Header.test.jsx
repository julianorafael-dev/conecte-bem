import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Header from '../../src/componentes/Header/Header.jsx';
import { getToken, setToken } from '../../src/services/auth.js';
import { RotaAtual, criarToken, renderizarComRotas, respostaFetch } from './utils.jsx';

function abrir() {
  renderizarComRotas({
    rotaInicial: '/Sobre',
    rotas: [
      { caminho: '/Sobre', elemento: <Header /> },
      { caminho: '/Home', elemento: <RotaAtual /> },
    ],
  });
}

describe('Header', () => {
  beforeEach(() => {
    localStorage.clear();
    global.fetch = jest.fn();
  });

  it('visitante vê o menu público e os links Entrar e Criar Conta', () => {
    abrir();
    const menu = screen.getByRole('navigation', { name: 'Navegação principal' });
    expect(menu).toHaveTextContent('Início');
    expect(menu).toHaveTextContent('Oportunidades');
    expect(screen.getByRole('link', { name: 'Entrar' })).toHaveAttribute('href', '/entrar');
    expect(screen.getByRole('link', { name: 'Criar Conta' })).toHaveAttribute('href', '/CriarConta');
    expect(document.title).toBe('Conecte-Bem');
    expect(fetch).not.toHaveBeenCalled();
  });

  it('ONG logada vê saudação com o nome, menu da ONG e título da aba', async () => {
    setToken(criarToken({ tipo: 'ONG' }));
    fetch.mockReturnValue(respostaFetch(200, { nome: 'ONG Esperança' }));
    abrir();
    expect(await screen.findByText('ONG Esperança')).toBeInTheDocument();
    expect(screen.getByRole('navigation', { name: 'Área da ONG' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Entrar' })).not.toBeInTheDocument();
    expect(fetch.mock.calls[0][0]).toMatch(/\/ongs\/me$/);
    expect(document.title).toBe('Conecte-Bem | ONG');
  });

  it('voluntário logado vê o menu do voluntário', async () => {
    setToken(criarToken({ tipo: 'VOLUNTARIO' }));
    fetch.mockReturnValue(respostaFetch(200, { nome: 'Maria' }));
    abrir();
    expect(await screen.findByText('Maria')).toBeInTheDocument();
    const menu = screen.getByRole('navigation', { name: 'Área do voluntário' });
    expect(menu).toHaveTextContent('Meu cadastro');
    expect(menu).toHaveTextContent('Minhas inscrições');
    expect(fetch.mock.calls[0][0]).toMatch(/\/voluntarios\/me$/);
    expect(document.title).toBe('Conecte-Bem | Voluntário');
  });

  it('se o nome não carregar, mostra apenas "Olá!"', async () => {
    setToken(criarToken({ tipo: 'VOLUNTARIO' }));
    fetch.mockRejectedValue(new TypeError('Failed to fetch'));
    abrir();
    expect(await screen.findByText('Olá!')).toBeInTheDocument();
  });

  it('Sair apaga a sessão e volta para a Home', async () => {
    setToken(criarToken({ tipo: 'ONG' }));
    fetch.mockReturnValue(respostaFetch(200, { nome: 'ONG Esperança' }));
    const usuario = userEvent.setup();
    abrir();
    await usuario.click(await screen.findByRole('button', { name: 'Sair' }));
    expect(screen.getByTestId('rota-atual')).toHaveTextContent('/Home');
    expect(getToken()).toBeNull();
  });
});
