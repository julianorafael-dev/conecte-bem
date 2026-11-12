import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Contato from '../../src/pages/Contato/Contato.jsx';
import { respostaFetch } from './utils.jsx';

async function preencher(usuario) {
  await usuario.type(screen.getByLabelText('Nome'), 'Maria');
  await usuario.type(screen.getByLabelText('E-mail'), 'maria@exemplo.com');
  await usuario.type(screen.getByLabelText('Mensagem'), 'Quero ser voluntária.');
}

describe('página de Contato', () => {
  beforeEach(() => {
    localStorage.clear();
    global.fetch = jest.fn();
  });

  it('todos os campos são obrigatórios', () => {
    render(<Contato />);
    expect(screen.getByLabelText('Nome')).toBeRequired();
    expect(screen.getByLabelText('E-mail')).toBeRequired();
    expect(screen.getByLabelText('Mensagem')).toBeRequired();
  });

  it('envia a mensagem, mostra sucesso e limpa o formulário', async () => {
    fetch.mockReturnValue(respostaFetch(204));
    const usuario = userEvent.setup();
    render(<Contato />);
    await preencher(usuario);
    await usuario.click(screen.getByRole('button', { name: 'Enviar' }));
    expect(await screen.findByRole('status')).toHaveTextContent('Mensagem enviada com sucesso!');
    expect(screen.getByLabelText('Nome')).toHaveValue('');
    const [url, opcoes] = fetch.mock.calls[0];
    expect(url).toMatch(/\/contato$/);
    expect(opcoes.method).toBe('POST');
    expect(JSON.parse(opcoes.body)).toEqual({ nome: 'Maria', email: 'maria@exemplo.com', mensagem: 'Quero ser voluntária.' });
  });

  it('desabilita o botão enquanto envia (evita envio duplicado)', async () => {
    let concluir;
    fetch.mockReturnValue(new Promise((resolve) => { concluir = resolve; }));
    const usuario = userEvent.setup();
    render(<Contato />);
    await preencher(usuario);
    await usuario.click(screen.getByRole('button', { name: 'Enviar' }));
    expect(screen.getByRole('button', { name: 'Enviando...' })).toBeDisabled();
    concluir({ status: 204, ok: true, json: () => Promise.resolve(null) });
    expect(await screen.findByRole('status')).toBeInTheDocument();
    expect(fetch).toHaveBeenCalledTimes(1);
  });

  it('mostra o erro do backend e mantém os dados digitados', async () => {
    fetch.mockReturnValue(respostaFetch(502, { mensagem: 'Não foi possível enviar sua mensagem. Tente novamente mais tarde.' }));
    const usuario = userEvent.setup();
    render(<Contato />);
    await preencher(usuario);
    await usuario.click(screen.getByRole('button', { name: 'Enviar' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível enviar sua mensagem.');
    expect(screen.getByLabelText('Nome')).toHaveValue('Maria');
  });
});
