# QA-009 — Dados coletados no cadastro

O cadastro envia e persiste os campos que antes eram descartados:

| Conta | Campo JSON | Persistência | Consulta |
|---|---|---|---|
| Voluntário | `cpf` | `usuarios.cpf`, somente dígitos | `GET /voluntarios/me` |
| Voluntário | `dataNascimento` | `usuarios.data_nascimento` | `GET /voluntarios/me` |
| Voluntário | `telefone` | `usuarios.telefone` | `GET /voluntarios/me` |
| ONG | `dataCriacao` | `ongs.data_criacao` | `GET /ongs/me` |
| ONG | `telefone` | `ongs.telefone`, já existente | `GET /ongs/me` |

Datas usam `YYYY-MM-DD`. CPF é opcional, mas, se informado, deve ter dígitos
verificadores válidos; aceita formato com pontuação ou apenas números. Datas
futuras e telefone com mais de 20 caracteres retornam HTTP 400.

Os campos permanecem anuláveis no banco para preservar contas existentes.
CPF e telefone são opcionais. Após o QA-010, nascimento é obrigatório para novos
voluntários na API e no formulário, com idade mínima de 18 anos. Veja
`docs/QA-010-IDADE-MINIMA.md` para o contrato atualizado e o acesso via Google.

CPF, nascimento e telefone do voluntário são expostos apenas no DTO do próprio
perfil autenticado. Não foram adicionados ao JWT nem à resposta genérica de
usuário usada pelo registro. No perfil, os novos campos são de consulta; editar
o nome preserva esses valores. A data de criação da ONG também é de consulta;
seu telefone continua editável como antes.

## Atualização do banco existente

Antes de iniciar esta versão do backend, execute no PostgreSQL o arquivo:

`backend/db/migrations/20261110_qa009_dados_cadastro.sql`

No pgAdmin, selecione a base do projeto, abra o Query Tool, carregue esse arquivo
e execute. A migração adiciona quatro colunas anuláveis, não remove registros e
pode ser executada novamente. O projeto não tem executor automático de migrações.
O `ddl-auto=validate` exige que essas colunas existam antes de iniciar o backend.

Para uma instalação nova, `backend/db/schema.sql` já contém as colunas.
Combine a incorporação desta migração com o participante responsável pelo banco.
Não execute o schema completo sobre uma base existente.

## Validação

`CadastroDadosHttpTest` cobre registro HTTP, gravação e releitura em H2 após
limpar o contexto JPA, consulta autenticada, isolamento entre contas, preservação
dos dados ao editar o nome, campos ausentes e rejeição de dados inválidos.

Validação em 10/11/2026:

- Suíte backend: 101 testes, zero falhas e erros, incluindo quatro testes novos.
- Frontend: build e lint aprovados.
- Chrome com API simulada: cadastro de voluntário e ONG envia os campos esperados;
  os perfis exibem CPF, datas e telefones retornados pela API. Essa checagem não
  substitui o fluxo integrado com PostgreSQL.

Comandos: `./mvnw.cmd test` na pasta backend; `npm run build` e `npm run lint`
na pasta frontend.

A aplicação da migração e o fluxo integrado no PostgreSQL local precisam ser
validados no ambiente da equipe antes de publicar. O script não foi aplicado
automaticamente ao banco local.
