# Integração da tela “Voluntários Inscritos”

> Status atualizado: o backend necessário para esta tela foi implementado na
> branch `feature/inscricoes-backend`. O detalhamento completo está em
> [INSCRICOES-BACKEND.md](INSCRICOES-BACKEND.md).

A tela do frontend usa a rota abaixo para mostrar os voluntários que se
candidataram a uma oportunidade da ONG autenticada.

## Endpoint disponível

`GET /oportunidades/{id}/inscricoes`

### Regras de acesso

- Exige JWT de uma conta do tipo `ONG`.
- A ONG precisa ser a proprietária da oportunidade.
- Voluntários, usuários não autenticados e outras ONGs recebem `403`.
- A regra específica da rota fica antes do `GET /oportunidades/**` público no
  `SecurityConfig`, para que nomes e e-mails de voluntários não fiquem expostos.

### Resposta de sucesso

```json
[
  {
    "id": 1,
    "usuarioId": 7,
    "nomeVoluntario": "Ana Souza",
    "emailVoluntario": "ana.souza@mail.com",
    "status": "PENDENTE",
    "dataInscricao": "2026-11-03T14:30:00"
  }
]
```

- Oportunidade sem inscritos: `[]` com HTTP 200.
- Oportunidade inexistente: HTTP 404.
- Inscrições canceladas também são exibidas com status `CANCELADA`, preservando
  o histórico da oportunidade.

## Dependências já atendidas

- A entidade, repositório, serviço, controller e DTO de inscrições existem no
  backend.
- `GET /ongs/me` já existe no módulo de ONG e é usado pelo painel para obter a
  ONG vinculada ao usuário autenticado.
- A tabela `inscricoes` já existe em `db/schema.sql`; não foi criada migração
  adicional nesta entrega.

## Integração do frontend

O frontend deve usar o token JWT e chamar essa rota ao abrir a tela
`/painel/oportunidades/:id/inscritos`. Os nomes `nomeVoluntario`,
`emailVoluntario` e `status` são mantidos para compatibilidade com a tela já
desenvolvida.
