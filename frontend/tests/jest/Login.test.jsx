import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Login from '../../src/pages/Login/Login.jsx';
import { getToken } from '../../src/services/auth.js';
import { RotaAtual, criarToken, renderizarComRotas, respostaFetch } from './utils.jsx';

function abrirLogin(estado) {
  return renderizarComRotas({
    rotaInicial: '/entrar',
    estado,
    rotas: [
      { caminho: '/entrar', elemento: <Login /> },
      { caminho: '/painel', elemento: <RotaAtual /> },
      { caminho: '/Home', elemento: <RotaAtual /> },
    ],
  });
}

async function preencherEEntrar(usuario, email, senha) {
  await usuario.type(screen.getByLabelText('E-mail'), email);
  await usuario.type(screen.getByLabelText('Senha'), senha);
  await usuario.click(screen.getByRole('button', { name: 'Acessar' }));
}

describe('página de Login', () => {
  beforeEach(() => {
    localStorage.clear();
    global.fetch = jest.fn();
  });

  it('mostra o formulário com título, campos e links', () => {
    abrirLogin();
    expect(screen.getByRole('heading', { level: 1, name: 'Acessar' })).toBeInTheDocument();
    expect(screen.getByLabelText('E-mail')).toBeRequired();
    expect(screen.getByLabelText('Senha')).toBeRequired();
    expect(screen.getByRole('link', { name: 'Cadastre-se' })).toHaveAttribute('href', '/CriarConta');
  });

  it('ONG faz login e vai para o painel', async () => {
    fetch.mockReturnValue(respostaFetch(200, { token: criarToken({ tipo: 'ONG' }), tipo: 'Bearer' }));
    const usuario = userEvent.setup();
    abrirLogin();
    await preencherEEntrar(usuario, '  ong@exemplo.com ', 'segredo123');
    expect(await screen.findByTestId('rota-atual')).toHaveTextContent('/painel');
    expect(getToken()).not.toBeNull();
    const [url, opcoes] = fetch.mock.calls[0];
    expect(url).toMatch(/\/auth\/login$/);
    expect(JSON.parse(opcoes.body)).toEqual({ email: 'ong@exemplo.com', senha: 'segredo123' });
  });

  it('voluntário faz login e vai para a Home', async () => {
    fetch.mockReturnValue(respostaFetch(200, { token: criarToken({ tipo: 'VOLUNTARIO' }) }));
    const usuario = userEvent.setup();
    abrirLogin();
    await preencherEEntrar(usuario, 'vol@exemplo.com', 'segredo123');
    expect(await screen.findByTestId('rota-atual')).toHaveTextContent('/Home');
  });

  it('mostra o erro do backend quando a senha está errada', async () => {
    fetch.mockReturnValue(respostaFetch(401, { mensagem: 'E-mail ou senha incorretos.' }));
    const usuario = userEvent.setup();
    abrirLogin();
    await preencherEEntrar(usuario, 'vol@exemplo.com', 'errada');
    expect(await screen.findByRole('alert')).toHaveTextContent('E-mail ou senha incorretos.');
    expect(screen.getByRole('button', { name: 'Acessar' })).toBeEnabled();
    expect(getToken()).toBeNull();
  });

  it('mostra mensagem quando o servidor está fora do ar', async () => {
    fetch.mockRejectedValue(new TypeError('Failed to fetch'));
    const usuario = userEvent.setup();
    abrirLogin();
    await preencherEEntrar(usuario, 'vol@exemplo.com', 'segredo123');
    expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível conectar ao servidor.');
  });

  it('quem já está logado é redirecionado', () => {
    localStorage.setItem('token', criarToken({ tipo: 'ONG' }));
    abrirLogin();
    expect(screen.getByTestId('rota-atual')).toHaveTextContent('/painel');
  });

  it('mostra a mensagem recebida de outra tela (ex.: conta criada)', () => {
    abrirLogin({ mensagem: 'Conta criada! Entre com seu e-mail e senha.' });
    expect(screen.getByRole('status')).toHaveTextContent('Conta criada!');
  });

  describe('Esqueci minha senha', () => {
    it('abre o modal, envia e mostra a confirmação', async () => {
      const usuario = userEvent.setup();
      abrirLogin();
      await usuario.click(screen.getByRole('button', { name: 'Esqueci minha senha' }));
      const dialogo = screen.getByRole('dialog', { name: 'Recuperar Senha' });
      expect(dialogo).toBeInTheDocument();
      await usuario.type(screen.getByLabelText('E-mail', { selector: '#recuperar-email' }), 'vol@exemplo.com');
      await usuario.type(screen.getByLabelText('Data de Nascimento'), '2000-01-01');
      await usuario.click(screen.getByRole('button', { name: 'Continuar' }));
      expect(screen.getByRole('status')).toHaveTextContent('Enviamos um link de recuperação para o e-mail informado.');
      expect(fetch).not.toHaveBeenCalled(); // recurso apenas visual, sem chamada ao backend
    });

    it('fecha o modal com Esc', async () => {
      const usuario = userEvent.setup();
      abrirLogin();
      await usuario.click(screen.getByRole('button', { name: 'Esqueci minha senha' }));
      await usuario.keyboard('{Escape}');
      await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
    });

    it('abre automaticamente quando vem do rodapé (state.recuperar)', () => {
      abrirLogin({ recuperar: true });
      expect(screen.getByRole('dialog', { name: 'Recuperar Senha' })).toBeInTheDocument();
    });
  });
});
