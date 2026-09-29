# Backend — Módulo ONG

Este documento descreve a implementação do módulo de ONGs no backend do ConecteBem e serve como guia de revisão e integração para a equipe.

## Escopo implementado

O módulo permite consultar, cadastrar e atualizar ONGs.

- `GET /ongs`: lista todas as ONGs.
- `GET /ongs/{id}`: busca uma ONG pelo identificador.
- `POST /ongs`: cadastra uma ONG usando o `usuarioId` de um usuário já existente.
- `PUT /ongs/{id}`: atualiza os dados de uma ONG existente.

## Estrutura do módulo

- `model/Ong`: entidade JPA mapeada para a tabela `ongs`.
- `repository/OngRepository`: acesso ao banco por meio de `JpaRepository`.
- `dto/OngRequestDTO`: dados recebidos pela API e regras de validação.
- `dto/OngResponseDTO`: dados devolvidos pela API, sem expor o vínculo interno `usuarioId`.
- `service/OngService`: regras de negócio, conversão entre entidade e DTO e operações no banco.
- `controller/OngController`: definição das rotas HTTP do módulo.

## Banco de dados

A tabela `ongs` possui os campos `id`, `usuario_id`, `nome`, `cnpj`, `descricao`, `telefone`, `cidade` e `estado`.

O relacionamento definido para o projeto é um para um: cada perfil de ONG pode ter uma única ONG. Por isso, além da chave estrangeira para `usuarios(id)`, o banco precisa manter a regra abaixo:

```sql
CONSTRAINT uq_ongs_usuario UNIQUE (usuario_id)
```

Também há unicidade para o CNPJ. A entidade `Ong` mantém essas duas regras com `unique = true`.

> Importante: a regra `uq_ongs_usuario` foi aplicada no banco local para refletir a decisão da equipe. Ela deve ser incluída no arquivo oficial de schema quando ele for versionado.

## Validações e respostas de erro

No cadastro e na atualização:

- `usuarioId`, `nome` e `cnpj` são obrigatórios;
- `nome`, `cnpj`, `telefone` e `cidade` possuem limites de tamanho;
- `estado`, quando informado, deve ter duas letras maiúsculas, por exemplo `RS`.

O tratamento global de erros retorna:

- `400 Bad Request` para dados inválidos;
- `404 Not Found` quando a ONG não existe;
- `409 Conflict` para CNPJ repetido ou tentativa de uma segunda ONG para o mesmo usuário;
- `500 Internal Server Error` para erros não previstos.

## Exemplo de cadastro

```json
{
  "usuarioId": 1,
  "nome": "ONG Exemplo",
  "cnpj": "12.345.678/0001-90",
  "descricao": "Projeto de exemplo.",
  "telefone": "(51) 99999-9999",
  "cidade": "Porto Alegre",
  "estado": "RS"
}
```

O `usuarioId` precisa existir na tabela `usuarios`, pois o banco possui chave estrangeira para esse registro.

## Configuração local do PostgreSQL

O backend usa a base local `conect-bem-db` em `localhost:5432`.

Para não versionar senhas, a configuração usa a variável de ambiente `DB_PASSWORD`:

```properties
spring.datasource.password=${DB_PASSWORD:postgres}
```

O valor padrão `postgres` atende ao `docker-compose.yml`. Em uma instalação local com outra senha, defina `DB_PASSWORD` na configuração de execução da IDE antes de iniciar a aplicação.

## Validações realizadas

- O schema foi importado no banco local `conect-bem-db`.
- As tabelas `usuarios`, `ongs`, `categorias`, `oportunidades` e `inscricoes` foram confirmadas.
- As constraints da tabela `ongs` foram conferidas, incluindo chave primária, chave estrangeira, CNPJ único e `usuario_id` único.
- O projeto compilou no IntelliJ.
- A aplicação iniciou e conectou ao PostgreSQL.
- `GET /health` respondeu `OK`.
- A suíte Maven executou com sucesso: 1 teste, 0 falhas e 0 erros.

## Dependências de integração

No estado atual, a configuração de segurança libera apenas `GET /health`; as rotas `/ongs` retornam `403` até a integração da autenticação/JWT. Isso não é um erro do módulo ONG: a requisição é bloqueada antes de chegar ao controller.

### Integração com a Pessoa 1 — segurança

- A configuração atual protege todas as rotas, exceto `/health`.
- Para utilizar o módulo ONG, a camada de segurança deve autenticar a requisição antes de ela chegar ao controller.
- Após a implementação do JWT, as rotas `/ongs` devem receber o usuário autenticado pelo token.
- O CORS já permite o frontend local em `http://localhost:5173`; outros endereços de frontend precisam ser adicionados pela configuração de segurança.

### Integração com a Pessoa 2 — usuários e autenticação

- O módulo de usuários deve criar um registro em `usuarios` com tipo `ong` antes de existir uma ONG.
- O ID desse usuário é o valor usado em `usuarioId` no `POST /ongs`.
- A tabela `ongs` possui chave estrangeira para `usuarios(id)` e `UNIQUE(usuario_id)`: um perfil de ONG só pode possuir uma ONG.
- No fluxo final, a equipe pode manter o `POST /ongs` ou criar a ONG na mesma transação do registro do usuário. Se optar pela segunda opção, deve reutilizar os campos e validações de `OngRequestDTO`.
- O `OngResponseDTO` não devolve `usuarioId`; após o login, o vínculo deve ser identificado pelo token, não por dados enviados pelo frontend.

### Contrato para demais módulos

- O módulo de oportunidades deve usar `ongs.id` como chave estrangeira em `oportunidades.ong_id`.
- Antes de criar oportunidade, a ONG precisa estar cadastrada.
- A exclusão de ONG não foi implementada no escopo atual. Como o schema usa `ON DELETE CASCADE`, remover uma ONG também pode remover oportunidades e inscrições relacionadas. Essa rota deve ser adicionada futuramente junto com regras de autorização, permitindo a exclusão apenas pela própria ONG ou por um administrador.
