# Módulo de inscrições — Pessoa 4

Este documento descreve o backend das candidaturas de voluntários a oportunidades.
Ele complementa as telas do frontend e não altera os módulos de autenticação,
cadastro de ONG ou administração de oportunidades.

## Rotas implementadas

| Método e rota | Quem pode acessar | Finalidade |
| --- | --- | --- |
| `POST /inscricoes` | Voluntário autenticado | Criar uma candidatura para uma oportunidade. |
| `GET /inscricoes/me` | Voluntário autenticado | Listar as próprias candidaturas. |
| `DELETE /inscricoes/{id}` | Voluntário dono da candidatura | Cancelar uma candidatura. |
| `GET /oportunidades/{id}/inscricoes` | ONG dona da oportunidade | Listar os voluntários inscritos. |

O JWT identifica o usuário em todas as rotas protegidas. O ID do voluntário ou
da ONG não é recebido no corpo da requisição, para evitar que uma pessoa execute
uma ação em nome de outra.

## Contratos usados pelo frontend

### Criar candidatura

`POST /inscricoes`

```json
{
  "oportunidadeId": 10
}
```

Em caso de sucesso, a API devolve `201 Created` com dados da candidatura e da
oportunidade, incluindo `tituloOportunidade`, `nomeOng`, `data`, `horario`,
`cidade`, `estado`, `descricaoOportunidade`, `statusInscricao` e
`statusOportunidade`.

### Minhas candidaturas

`GET /inscricoes/me` devolve uma lista com o mesmo formato da resposta de criação.
Quando o voluntário ainda não possui candidaturas, devolve uma lista vazia (`[]`).

### Voluntários inscritos

`GET /oportunidades/{id}/inscricoes` devolve uma lista no formato:

```json
[
  {
    "id": 1,
    "usuarioId": 7,
    "nomeVoluntario": "Ana Souza",
    "emailVoluntario": "ana@exemplo.com",
    "status": "PENDENTE",
    "dataInscricao": "2026-11-03T14:30:00"
  }
]
```

Inscrições canceladas também aparecem nessa consulta, identificadas pelo status,
para que a ONG mantenha o histórico das candidaturas.

## Regras de negócio

- Apenas usuários do tipo `VOLUNTARIO` podem criar ou cancelar candidaturas.
- Apenas uma inscrição ativa (`PENDENTE` ou `CONFIRMADA`) é permitida por
  voluntário e oportunidade.
- A oportunidade precisa estar com status `ABERTA`.
- A quantidade de inscrições ativas não pode ultrapassar `oportunidade.vagas`.
- A candidatura cancelada deixa de ocupar vaga. O mesmo voluntário pode se
  candidatar novamente: o registro anterior é reativado como `PENDENTE`, em vez
  de criar uma segunda linha e violar a regra única do banco.
- Somente a ONG proprietária pode consultar inscritos da própria oportunidade.
- A propriedade é conferida no serviço, além da regra de papel no
  `SecurityConfig`; assim a proteção não depende apenas da rota HTTP.

## Concorrência

No momento da candidatura, o repositório bloqueia a linha da oportunidade com
`PESSIMISTIC_WRITE` antes de contar vagas e salvar a inscrição. Isso serializa
requisições concorrentes para a mesma oportunidade e impede que duas pessoas
ocupem simultaneamente a última vaga.

## Estrutura criada

- `model/Inscricao` e `model/StatusInscricao`
- `repository/InscricaoRepository`
- DTOs de entrada e saída do módulo
- `service/InscricaoService`
- `controller/InscricaoController`
- `InscricaoConflictException`, tratada como HTTP `409 Conflict`
- Testes unitários das regras principais em `InscricaoServiceTest`

## Dependências de integração

- A tabela `inscricoes` já existe no schema do projeto; não foi necessária uma
  nova migração.
- O frontend deve enviar somente `oportunidadeId` ao criar a candidatura e
  armazenar o JWT na chave de token definida pelo módulo de autenticação.
- O fluxo visual de candidaturas e de voluntários inscritos pertence ao frontend
  da Pessoa 5. Este módulo fornece as rotas e os campos necessários para ele.
- A candidatura entra como `PENDENTE` e a ONG proprietária da oportunidade
  confirma ou cancela na tela de voluntários inscritos:
  - `PUT /inscricoes/{id}/confirmar`: `PENDENTE` -> `CONFIRMADA`. Uma inscrição
    `CANCELADA` não pode ser confirmada (HTTP 409).
  - `PUT /inscricoes/{id}/recusar`: `PENDENTE` ou `CONFIRMADA` -> `CANCELADA`,
    liberando a vaga.
  - Ambas exigem conta ONG e devolvem o inscrito atualizado. Uma ONG não altera
    inscrições de oportunidades de outra ONG (HTTP 403).
- `PENDENTE` e `CONFIRMADA` ocupam vaga da mesma forma.

## Como validar

1. Execute o backend com banco PostgreSQL configurado.
2. Autentique um voluntário e faça `POST /inscricoes` para uma oportunidade aberta
   com vagas.
3. Consulte `GET /inscricoes/me` com o mesmo token.
4. Cancele com `DELETE /inscricoes/{id}` e confirme que uma nova candidatura é
   permitida.
5. Autentique a ONG proprietária e consulte
   `GET /oportunidades/{id}/inscricoes`.
6. Repita a última consulta com outra ONG ou voluntário e confirme o retorno `403`.

## Validação realizada nesta branch

- Backend iniciado com PostgreSQL e Hibernate reconhecendo a entidade
  `Inscricao` e o repositório correspondente.
- `InscricaoServiceTest`: 11 testes unitários aprovados no IntelliJ com JDK 21.
  Eles cobrem criação, reinscrição, duplicidade, limite de vagas, oportunidade
  fechada, permissões, cancelamento e propriedade da oportunidade.
