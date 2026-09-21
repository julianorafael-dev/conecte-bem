# Backend - Conecte Bem

Backend em Java + Spring Boot da plataforma que conecta ONGs a voluntários.

## Pré-requisitos

- Java 21 (JDK)
- IntelliJ IDEA
- Docker Desktop (Windows: precisa do WSL2 ativado — o instalador do Docker Desktop configura isso automaticamente na maioria dos casos)
- Git

## Como rodar o projeto localmente

### 1. Clonar e entrar na branch develop

No IntelliJ: File → New → Project from Version Control, cole a URL do repositório. Depois de clonado, troque a branch de main para develop (clique no nome da branch no topo → Remote Branches → develop → Checkout).

Crie sua própria branch a partir da develop para trabalhar:
Git → Branches... → develop → New Branch from Selected (nomeie como feature/sua-tarefa).

### 2. Subir o banco de dados local (Docker)

Abra o Docker Desktop uma vez (deixe ele rodando em segundo plano). Depois, no terminal, navegue até a pasta backend do projeto e rode:

docker compose up -d

Isso baixa e sobe dois containers: o banco Postgres (porta 5432) e o pgAdmin, uma interface visual pra ver as tabelas (porta 5050). Na primeira vez demora um pouco (baixando as imagens).

Para conferir se subiu: docker ps deve listar dois containers rodando.

### 3. Ver as tabelas pelo pgAdmin (opcional, mas recomendado)

Abra http://localhost:5050 no navegador. Login: admin@conectebem.com, senha admin.

Clique com botão direito em "Servers" → "Register" → "Server...". Na aba General, dê um nome (ex: "Conecte Bem Local"). Na aba Connection:
- Host name/address: db (não localhost — é o nome do serviço dentro da rede do Docker)
- Port: 5432
- Maintenance database: conect-bem-db
- Username: postgres
- Password: postgres

### 4. Rodar o backend no IntelliJ

O arquivo application.properties (em backend/src/main/resources/) já vem configurado para conectar no banco local — não precisa mexer em nada nele.

Clique no botão ▶ verde na classe BackendApplication. Se pedir para criar uma Run Configuration, aceite o padrão.

### 5. Testar se está tudo funcionando

Acesse http://localhost:8080/health no navegador. Deve aparecer a palavra ok.

## Estrutura do projeto

backend/src/main/java/com/conectebem/backend/
├── controller/     → endpoints da API
├── service/        → regras de negócio
├── repository/     → acesso ao banco (Spring Data JPA)
├── model/          → entidades JPA
├── dto/            → objetos de entrada/saída da API
├── config/         → CORS, Security
└── exception/      → tratamento de erros

## Observações importantes

- O banco roda localmente via Docker durante o desenvolvimento. O banco da VPS fica reservado para quando o projeto for para produção — não se conecta nele agora.
- spring.jpa.hibernate.ddl-auto=update está configurado — o Hibernate cria/atualiza as tabelas automaticamente a partir das classes de entidade (@Entity) que vocês forem criando. Não precisa rodar CREATE TABLE manualmente para as entidades do backend.
- As 5 tabelas do modelo (usuarios, ongs, categorias, oportunidades, inscricoes) já existem no banco de estudo/design no pgAdmin — suas entidades Java devem ter os mesmos nomes de tabela e coluna para bater certinho.
- Se o Docker parar de rodar (reiniciou o PC, por exemplo), é só rodar docker compose up -d de novo dentro da pasta backend — os dados salvos continuam lá (não se perdem).
- Para parar os containers sem perder dados: docker compose stop. Para remover tudo (inclusive os dados): docker compose down -v.

## Fluxo de Git

- Nunca commite direto na main ou na develop.
- Crie uma branch feature/nome-da-tarefa a partir da develop.
- Ao terminar, abra um Pull Request para develop. Peça para outra pessoa revisar antes do merge.

## Windows: avisos importantes

- Docker Desktop no Windows exige o WSL2. Se alguém nunca usou Docker antes, o instalador geralmente já configura isso sozinho, mas pode pedir reinício do PC no meio do processo — é normal.
- Virtualização precisa estar ativada na BIOS. Se o Docker Desktop reclamar de "virtualization not enabled", a pessoa precisa entrar na BIOS do PC (geralmente F2 ou Del no boot) e ativar "Intel VT-x" ou "AMD-V", dependendo do processador.
- Antivírus corporativo/Windows Defender às vezes barra a instalação do Docker — se alguém tiver esse problema, me chama que resolvemos individualmente.
