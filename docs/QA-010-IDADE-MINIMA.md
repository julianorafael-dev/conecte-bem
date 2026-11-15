# QA-010 — Idade mínima no cadastro

- Novos voluntários precisam informar nascimento e ter 18 anos completos.
- O campo limita a seleção à data de hoje menos 18 anos e mostra mensagem de
  validação; o envio também verifica a idade.
- `POST /auth/registro` rejeita nascimento ausente, nulo ou abaixo da idade mínima
  com HTTP 400, antes de persistir. Clientes devem enviar `dataNascimento`.
- Frontend e backend usam a data civil de `America/Sao_Paulo`.
- Quem completa 18 anos hoje é aceito. Nascidos em 29/02 atingem o limite em 01/03
  nos anos sem 29/02, conforme a comparação de datas adotada pelo projeto.
- ONG pode informar criação até hoje, sem restrição de idade de 18 anos.
- Contas existentes continuam acessando normalmente; não há nova migração SQL.
- Como o token Google não fornece nascimento, `/auth/google` autentica somente
  contas existentes. Um novo usuário deve primeiro preencher o formulário de
  cadastro; a API retorna HTTP 400 com essa orientação, sem criar conta automática.

Validação: testes de limite de aniversário e anos bissextos, chamadas HTTP com
data ausente/nula/menor de idade, cadastro no aniversário e ONG criada hoje.
No frontend, executar `node --test tests/idade.test.mjs`, `npm run build` e
`npm run lint`. No backend, executar `./mvnw.cmd test`.
