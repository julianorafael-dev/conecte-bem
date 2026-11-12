# Relatório de Testes Automatizados (JUnit e Jest): Conecte-Bem

- **Data:** 11/11/2026
- **Branch:** `teste-Jest`, criada a partir da `develop` no commit `b896f6a` (PR #50)
- **Ambiente:** Windows 11, Java 21, Maven Wrapper, Node 24.15, Jest 30.5.2, banco H2 em memória (usado só pelos testes do backend)
- **Escopo:** execução dos testes JUnit do backend; instalação, configuração e criação dos testes Jest do frontend. Nenhum código da aplicação foi alterado.

---

## Resumo

| Suíte | Ferramenta | Testes | Passaram | Falharam | Tempo | Resultado |
|---|---|---|---|---|---|---|
| Backend | JUnit 5 + Spring Boot Test (`./mvnw test`) | 113 | 113 | 0 | 1 min 28 s | **BUILD SUCCESS** |
| Frontend | **Jest 30** + Testing Library (`npm run test:jest`) | 43 | 43 | 0 | cerca de 18 s | **Passou** |
| Frontend | Runner nativo do Node (`npm test`), preexistente | 2 | 2 | 0 | 0,6 s | Passou |

**Conclusão:** todas as suítes passaram, **158 testes no total, sem falhas**.

---

## 1. JUnit (backend)

**Comando:** `cd backend && ./mvnw.cmd test`
**Resultado:** `Tests run: 113, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`

### Resultado por classe

| Classe de teste | Tipo | Testes | Falhas | Tempo |
|---|---|---|---|---|
| `BackendApplicationTests` | Integração (sobe o contexto Spring) | 1 | 0 | 26,0 s |
| `config.SecurityAuthorizationHttpTest` | HTTP (MockMvc) | 5 | 0 | 7,8 s |
| `config.SecurityConfigHttpTest` | HTTP (MockMvc) | 22 | 0 | 6,9 s |
| `security.JwtServiceTest` | Unitário | 4 | 0 | 0,08 s |
| `service.AuthServiceTest` | Unitário (Mockito) | 12 | 0 | 0,7 s |
| `service.CadastroDadosHttpTest` | HTTP + H2 | 5 | 0 | 6,1 s |
| `service.ContatoServiceTest` | Unitário (Mockito) | 2 | 0 | 0,16 s |
| `service.IdadeMinimaTest` | Unitário | 2 | 0 | 0,01 s |
| `service.InscricaoServiceTest` | Unitário (Mockito) | 16 | 0 | 1,1 s |
| `service.OngServiceTest` | Unitário (Mockito) | 7 | 0 | 0,06 s |
| `service.OportunidadeServiceHttpTest` | HTTP + H2 | 18 | 0 | 2,3 s |
| `service.OportunidadeServiceTest` | Unitário (Mockito) | 17 | 0 | 0,16 s |
| `service.VoluntarioServiceTest` | Unitário (Mockito) | 2 | 0 | 0,02 s |
| **Total** | | **113** | **0** | **1 min 28 s** |

### O que está coberto
- **Autenticação:** cadastro de voluntário e ONG, CNPJ inválido ou duplicado, login, senha com BCrypt, idade mínima de 18 anos, dados pessoais (CPF, datas, telefone).
- **JWT:** geração, validação e expiração do token, token adulterado ou de usuário inexistente.
- **Autorização:** permissões de categorias e de ONG por perfil; voluntário não escreve em oportunidades; perfil errado recebe 403 sem perder a sessão; método não suportado recebe 405; sem token recebe 401.
- **Inscrições:** criar, impedir duplicidade, lotação, oportunidade encerrada, cancelar, reativar, confirmação e cancelamento pela ONG.
- **Oportunidades:** CRUD, ONG dona, filtros, validações e rotas HTTP.
- **ONG, voluntário e contato:** perfis e envio de e-mail (com mock do SMTP).

### Lacunas observadas
- **`CategoriaService`:** sem teste das regras de negócio (nome duplicado, exclusão de categoria em uso).
- **`POST /contato`:** sem teste HTTP das validações.
- **`GoogleTokenService` e `POST /auth/google`:** sem testes.
- **Cobertura em percentual:** não medida, porque o projeto não tem JaCoCo.

### Avisos durante a execução (não são falhas)
- `spring.jpa.open-in-view is enabled by default`.
- `H2Dialect does not need to be specified explicitly`.
- Aviso do Mockito/Byte Buddy sobre "Java agent loaded dynamically", comum no Java 21.

---

## 2. Jest (frontend)

### Instalação e configuração
O projeto não tinha Jest. Foi feito o seguinte:

| Item | Descrição |
|---|---|
| Dependências (só de desenvolvimento) | `jest`, `jest-environment-jsdom`, `babel-jest`, `@babel/core`, `@babel/preset-env`, `@babel/preset-react`, `babel-plugin-transform-vite-meta-env`, `@testing-library/react`, `@testing-library/dom`, `@testing-library/jest-dom`, `@testing-library/user-event`, `identity-obj-proxy` |
| `frontend/jest.config.js` | Ambiente jsdom, Babel para JSX e `import.meta.env` (configurado só para o Jest, sem afetar o Vite), mapeamento de CSS e imagens |
| `frontend/jest.setup.js` | Matchers do Testing Library e um ajuste de `TextEncoder`, exigido pelo React Router 7 |
| `frontend/tests/__mocks__/arquivoMock.js` | Substitui imagens nos testes |
| `frontend/package.json` | Novo script `test:jest`. O `test` existente continua igual. |
| `frontend/eslint.config.js` | Libera `describe`, `it`, `expect` e `jest` nos arquivos de teste |

**Como executar:** `cd frontend && npm run test:jest`

### Resultado
**43 testes em 6 arquivos: 43 passaram, 0 falharam.**

| Arquivo (`frontend/tests/jest/`) | Testes | O que verifica |
|---|---|---|
| `auth.test.jsx` | 7 | Salvar, ler e remover o token; ler os dados do JWT (inclusive e-mail com acento); token expirado ou malformado; `isOng` e `isVoluntario` |
| `api.test.jsx` | 10 | URL padrão da API; envio do corpo em JSON; token no cabeçalho; rotas `/auth/*` sem token; resposta 204; servidor fora do ar; 401 encerra a sessão; mensagens de erro 401, 403, 409 e 500 |
| `rotasProtegidas.test.jsx` | 7 | Visitante vai para o login; ONG e voluntário acessam só a própria área; token vencido é tratado como visitante |
| `Login.test.jsx` | 10 | Formulário e campos obrigatórios; login da ONG vai para o painel e o do voluntário para a Home; senha errada; servidor fora do ar; usuário logado é redirecionado; mensagem vinda de outra tela; "Esqueci minha senha" (abrir, enviar, fechar com Esc, abrir pelo rodapé) |
| `Contato.test.jsx` | 4 | Campos obrigatórios; envio com sucesso e formulário limpo; botão desabilitado durante o envio; mensagem de erro mantendo os dados digitados |
| `Header.test.jsx` | 5 | Menu do visitante; ONG e voluntário com saudação, menu próprio e título da aba; nome que não carrega; botão Sair |
| `utils.jsx` | — | Funções auxiliares dos testes (gerar token falso, simular o `fetch`, renderizar com rotas) |

### Cobertura das partes testadas

| Arquivo | % Linhas | % Branches |
|---|---|---|
| `services/api.js` | 100 | 100 |
| `services/auth.js` | 100 | 100 |
| `routes/RotaONG.jsx` e `RotaVoluntario.jsx` | 100 | 100 |
| `componentes/Header/Header.jsx`, `MenuOng.jsx`, `MenuVoluntario.jsx` | 100 | 50 a 100 |
| `pages/Contato/Contato.jsx` | 100 | 87,5 |
| `context/AuthContext.jsx` | 93,8 | 60 |
| `pages/Login/Login.jsx` | 84,6 | 71,1 |
| **Média dos arquivos testados** | **90** | **82,5** |

### Lacunas observadas
Ainda não têm teste Jest: Criar Conta, Oportunidades (listagem e inscrição), Painel da ONG, Gerenciar Oportunidades, Formulário de Oportunidade, Voluntários Inscritos, Minhas Candidaturas, os perfis da ONG e do voluntário, Home, Sobre e Como Funciona.

### Observações
- O teste preexistente `frontend/tests/idade.test.mjs` usa o runner nativo do Node (`npm test`) e continua passando (2 de 2).
- Os novos testes usam a extensão `.test.jsx` para não serem pegos pelo `node --test`.
- O lint (`npm run lint`) acusa 1 erro **preexistente**, sem relação com os testes: `src/pages/Login/Login.jsx:30`, regra `react-hooks/set-state-in-effect`.
- O `npm audit` acusa 22 vulnerabilidades baixas ou moderadas, a maioria em dependências do próprio Jest. Nenhuma é alta ou crítica.

---

## 3. Próximos passos sugeridos
1. Criar testes Jest para Criar Conta e Oportunidades (inscrição), que são os fluxos principais ainda sem teste.
2. No backend, testar `CategoriaService` e `POST /contato`, e configurar JaCoCo para medir a cobertura.
3. Corrigir o erro de lint preexistente em `Login.jsx`.
