import { screen } from '@testing-library/react';
import RotaONG from '../../src/routes/RotaONG/RotaONG.jsx';
import RotaVoluntario from '../../src/routes/RotaVoluntario/RotaVoluntario.jsx';
import { setToken } from '../../src/services/auth.js';
import { RotaAtual, criarToken, renderizarComRotas } from './utils.jsx';

function abrir(rotaInicial) {
  renderizarComRotas({
    rotaInicial,
    rotas: [
      { caminho: '/entrar', elemento: <RotaAtual /> },
      { caminho: '/painel', elemento: <RotaONG><p>Área da ONG</p></RotaONG> },
      { caminho: '/painel/candidaturas', elemento: <RotaVoluntario><p>Área do voluntário</p></RotaVoluntario> },
    ],
  });
}

describe('rotas protegidas', () => {
  beforeEach(() => localStorage.clear());

  it('visitante é levado ao login nas duas áreas', () => {
    abrir('/painel');
    expect(screen.getByTestId('rota-atual')).toHaveTextContent('/entrar');
  });

  it('visitante é levado ao login na área do voluntário', () => {
    abrir('/painel/candidaturas');
    expect(screen.getByTestId('rota-atual')).toHaveTextContent('/entrar');
  });

  it('ONG acessa a área da ONG', () => {
    setToken(criarToken({ tipo: 'ONG' }));
    abrir('/painel');
    expect(screen.getByText('Área da ONG')).toBeInTheDocument();
  });

  it('ONG não acessa a área do voluntário', () => {
    setToken(criarToken({ tipo: 'ONG' }));
    abrir('/painel/candidaturas');
    expect(screen.queryByText('Área do voluntário')).not.toBeInTheDocument();
    expect(screen.getByTestId('rota-atual')).toHaveTextContent('/entrar');
  });

  it('voluntário acessa a área do voluntário', () => {
    setToken(criarToken({ tipo: 'VOLUNTARIO' }));
    abrir('/painel/candidaturas');
    expect(screen.getByText('Área do voluntário')).toBeInTheDocument();
  });

  it('voluntário não acessa a área da ONG', () => {
    setToken(criarToken({ tipo: 'VOLUNTARIO' }));
    abrir('/painel');
    expect(screen.queryByText('Área da ONG')).not.toBeInTheDocument();
  });

  it('token expirado é tratado como visitante', () => {
    setToken(criarToken({ tipo: 'ONG', expiraEmSegundos: -10 }));
    abrir('/painel');
    expect(screen.getByTestId('rota-atual')).toHaveTextContent('/entrar');
    expect(localStorage.getItem('token')).toBeNull();
  });
});
