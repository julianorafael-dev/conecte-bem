# Backend: o que falta para a tela "Voluntários Inscritos"

A tela do frontend (`/painel/oportunidades/:id/inscritos`) já está pronta na branch
`feature/voluntarios-inscritos`. Para ela funcionar, o backend precisa de dois ajustes.

## 1. Endpoint de inscrições (não existe)

A tabela `inscricoes` já existe em `db/schema.sql`, mas não há entidade, repositório,
serviço nem controller para ela.

**Rota:** `GET /oportunidades/{id}/inscricoes`

**Resposta (200):** lista de objetos neste formato. O frontend usa exatamente esses nomes:

```json
[
  { "id": 1, "nomeVoluntario": "Ana Souza", "emailVoluntario": "ana.souza@mail.com", "status": "CONFIRMADA" }
]
```

- `id`: id da inscrição (`inscricoes.id`)
- `nomeVoluntario` / `emailVoluntario`: vêm de `usuarios.nome` / `usuarios.email` (via `inscricoes.usuario_id`)
- `status`: `PENDENTE`, `CONFIRMADA` ou `CANCELADA` (a tela mostra todas, com o status ao lado)
- Oportunidade sem inscritos: devolver `[]`
- Oportunidade inexistente: 404 (`ResourceNotFoundException`)

**O que criar:** entidade `Inscricao` (tabela `inscricoes`), `InscricaoRepository`
(ex.: `findByOportunidadeId`), serviço, DTO de resposta e o método no controller
(pode ficar no `OportunidadeController` ou em um `InscricaoController`).

## 2. Segurança do endpoint (importante)

No `SecurityConfig`, hoje `GET /oportunidades/**` é **público** (`permitAll`). Se o novo
endpoint ficar sob essa regra, qualquer pessoa, mesmo sem login, vai ver nome e e-mail
dos voluntários.

Ajustes necessários:
- Liberar o `permitAll` só para `GET /oportunidades` e `GET /oportunidades/{id}`, e exigir
  login em `GET /oportunidades/*/inscricoes` (a regra mais específica deve vir **antes** da genérica).
- No serviço, conferir se o usuário logado é `ONG` **e** dono da oportunidade
  (`oportunidade.ong.usuarioId == usuarioLogado.id`). Caso contrário, devolver 403.

## 3. Endpoint `GET /ongs/me` (não existe)

As telas `GerenciarOportunidades`, `FormOportunidade` e `VoluntariosInscritos` chamam
`GET /ongs/me` para saber qual é a ONG do usuário logado. No `OngController` atual só existem
`GET /ongs`, `GET /ongs/{id}`, `POST` e `PUT`, então `/ongs/me` falha.

- Deve devolver a ONG do usuário logado (`OngResponseDTO`, com o campo `id`), buscando por
  `ongs.usuario_id`.
- Ficar declarado **antes** de `/{id}` no controller (ou usar mapeamento que não conflite).
- Se o usuário logado não for ONG ou não tiver ONG cadastrada: 404 ou 403.

## Observações

- Se alguém já tiver o `/ongs/me` em outra branch, basta aproveitar.
- Se for necessário mudar os nomes dos campos da resposta, avisar o front para ajustar
  `VoluntariosInscritos.jsx`.
