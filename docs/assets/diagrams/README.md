# Diagramas

Os diagramas são escritos em **Mermaid** e mantidos em arquivos `.mmd` para reutilização e versionamento. Os mesmos diagramas estão incorporados nos READMEs por meio de blocos `mermaid`.

| Arquivo | Conteúdo | Onde aparece |
|---|---|---|
| `arquitetura-geral.mmd` | Visão geral cliente, API, banco e integrações | README principal |
| `fluxo-comunicacao.mmd` | Comunicação frontend → backend → banco | README principal |
| `camadas-backend.mmd` | Organização das camadas do backend | `backend/README.md` |
| `fluxo-autenticacao.mmd` | Login, JWT e autorização | `backend/README.md` |
| `modelo-dados.mmd` | Relacionamento das entidades | README principal e backend |
| `estado-inscricao.mmd` | Ciclo de vida de uma inscrição | `backend/README.md` |

## Validação

Os diagramas foram construídos a partir do código-fonte existente. Para validar ou editar visualmente:

1. Abra o conteúdo do arquivo `.mmd` em um editor compatível com Mermaid.
2. Confira se os nomes de tabelas, entidades e rotas continuam iguais aos do código.
3. Ao alterar um diagrama, atualize também o bloco correspondente no README.

> Regra do projeto: o diagrama deve representar a arquitetura **real**, não uma proposta idealizada.
