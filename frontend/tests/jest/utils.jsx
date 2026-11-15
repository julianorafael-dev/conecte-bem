// Utilitários compartilhados pelos testes Jest.
import { render } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { AuthProvider } from '../../src/context/AuthContext.jsx';

function base64Url(objeto) {
  return Buffer.from(JSON.stringify(objeto)).toString('base64')
    .replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_');
}

// Gera um JWT com o mesmo formato do backend (a assinatura não é verificada no frontend).
export function criarToken({ id = 1, email = 'teste@exemplo.com', tipo = 'VOLUNTARIO', expiraEmSegundos = 3600 } = {}) {
  const exp = Math.floor(Date.now() / 1000) + expiraEmSegundos;
  return `${base64Url({ alg: 'HS256', typ: 'JWT' })}.${base64Url({ iss: 'conecte-bem-api', sub: email, id, tipo, exp })}.assinatura`;
}

// Resposta falsa no formato do fetch.
export function respostaFetch(status, corpo) {
  return Promise.resolve({
    status,
    ok: status >= 200 && status < 300,
    json: () => (corpo === undefined ? Promise.reject(new Error('sem corpo')) : Promise.resolve(corpo)),
  });
}

// Mostra a rota atual na tela, para os testes conferirem redirecionamentos.
export function RotaAtual() {
  const { pathname } = useLocation();
  return <p data-testid="rota-atual">{pathname}</p>;
}

// Renderiza um elemento dentro do AuthProvider e de um roteador em memória.
export function renderizarComRotas({ rotaInicial = '/', estado, rotas }) {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[{ pathname: rotaInicial, state: estado }]}>
        <Routes>
          {rotas.map(({ caminho, elemento }) => <Route key={caminho} path={caminho} element={elemento} />)}
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
}
