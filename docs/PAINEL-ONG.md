# Painel da ONG

Documentação da tarefa **Pessoa 4 — Painel da ONG**, implementada na branch `feature/painel-ong`.

## Objetivo

Criar uma área inicial para a ONG autenticada acompanhar suas oportunidades e voluntários inscritos, com acesso direto às ações mais usadas.

## O que foi implementado

### Backend

Foi criada a rota abaixo:

```http
GET /ongs/me/painel
Authorization: Bearer <token de uma ONG>
```

Ela retorna um resumo consolidado da ONG que está autenticada:

```json
{
  "nomeOng": "ONG Esperança",
  "totalOportunidades": 8,
  "oportunidadesAtivas": 5,
  "oportunidadesEncerradas": 2,
  "totalVoluntariosInscritos": 24
}
```

A rota não recebe `ongId` por parâmetro. O usuário é obtido do JWT e a ONG é localizada por `ongs.usuario_id`. Essa decisão evita que uma ONG consulte os indicadores de outra organização alterando a requisição manualmente.

O caminho `/ongs/me/**` foi protegido para o papel `ONG` antes da regra pública de leitura de ONGs.

### Regras dos indicadores

- **Total de oportunidades:** todas as oportunidades da ONG, inclusive canceladas.
- **Oportunidades ativas:** status `ABERTA`.
- **Oportunidades encerradas:** status `ENCERRADA`.
- **Voluntários inscritos:** status `PENDENTE` ou `CONFIRMADA`.

Inscrições com status `CANCELADA` foram excluídas da métrica de voluntários, pois não representam participação ativa.

As oportunidades são contadas por métodos derivados do Spring Data. Para inscrições, foi usada uma consulta SQL nativa no `OportunidadeRepository`, porque a tabela `inscricoes` já existe no schema, mas ainda não há entidade JPA própria para esse módulo.

### Frontend

A nova página está em:

```text
frontend/src/pages/PainelOng/
```

Essa organização segue a convenção acordada pela equipe: telas completas ficam em `pages`; componentes reutilizáveis ficam em `components`.

Rota criada:

```text
/painel
```

Ela é protegida por `RotaONG`. A tela apresenta:

- saudação com o nome da ONG;
- quatro indicadores do resumo;
- botão para criar oportunidade;
- até três oportunidades mais recentes da própria ONG;
- atalhos para gerenciamento de oportunidades e edição do perfil;
- estados de carregamento, erro e lista vazia;
- layout responsivo para desktop, tablet e celular.

O Header mostra o link **Painel da ONG** somente quando o token local identifica uma conta do tipo `ONG`.

## Arquivos da tarefa

- `backend/src/main/java/com/conectebem/backend/dto/PainelOngResponseDTO.java`
- `backend/src/main/java/com/conectebem/backend/service/OngService.java`
- `backend/src/main/java/com/conectebem/backend/repository/OportunidadeRepository.java`
- `backend/src/main/java/com/conectebem/backend/controller/OngController.java`
- `backend/src/main/java/com/conectebem/backend/config/SecurityConfig.java`
- `backend/src/test/java/com/conectebem/backend/service/OngServiceTest.java`
- `frontend/src/pages/PainelOng/PainelOng.jsx`
- `frontend/src/pages/PainelOng/PainelOng.css`
- `frontend/src/App.jsx`
- `frontend/src/componentes/Header/Header.jsx`

## Validações realizadas

- Build do backend concluído no IntelliJ com Java 21.
- Build do frontend concluído com Vite.
- Lint do frontend concluído sem erros.
- Testes unitários incluídos para o resumo do painel e para ONG inexistente.
- A validação integrada com JWT e PostgreSQL será realizada no ambiente local completo antes da pull request.

## Dependências de integração

- A ONG precisa existir no cadastro complementar para que `GET /ongs/me` e o painel localizem seu perfil.
- O frontend depende do token JWT salvo no `localStorage` com a chave `token`.
- O indicador de inscritos depende da tabela `inscricoes` já presente em `backend/db/schema.sql`.
- A listagem detalhada de voluntários inscritos continua sendo responsabilidade da funcionalidade específica de inscrições.
