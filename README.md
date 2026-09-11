# 🤝 ConecteBem

### Plataforma web para conectar ONGs e voluntários

O **ConecteBem** é uma plataforma web desenvolvida para aproximar ONGs e pessoas interessadas em participar de ações voluntárias.

O sistema permitirá que organizações publiquem oportunidades de voluntariado e que voluntários encontrem ações sociais para participar.

---

## 🎯 Objetivo do Projeto

O projeto tem como objetivo aplicar os conhecimentos adquiridos durante o curso de Desenvolvimento Full Stack, utilizando tecnologias de Front-end, Back-end, banco de dados, controle de versão, testes e documentação.

---

# 💡 Funcionalidades

## 👤 Visitante

- Visualizar a página inicial
- Conhecer o projeto
- Visualizar oportunidades disponíveis
- Realizar cadastro
- Realizar login

## 🤝 Voluntário

- Visualizar perfil
- Editar perfil
- Visualizar oportunidades
- Ver detalhes das oportunidades
- Inscrever-se em uma oportunidade
- Cancelar inscrição
- Visualizar minhas inscrições

## 🏢 ONG

- Visualizar perfil
- Editar perfil
- Cadastrar oportunidades
- Editar oportunidades
- Excluir oportunidades
- Visualizar voluntários inscritos

---

# 🛠️ Tecnologias

## Front-end

- React
- JavaScript
- HTML
- CSS
- React Router

## Back-end

- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT

## Banco de Dados

- PostgreSQL

## Ferramentas

- Git
- GitHub
- VS Code
- IntelliJ IDEA
- Postman
- Maven

---

# 🏗️ Estrutura do Projeto

A estrutura abaixo representa a arquitetura planejada para o projeto.

```text
conecte-bem/
│
├── frontend/
│   │
│   ├── public/
│   │
│   ├── src/
│   │   │
│   │   ├── assets/
│   │   │   ├── images/
│   │   │   └── icons/
│   │   │
│   │   ├── components/
│   │   │   ├── Header/
│   │   │   ├── Footer/
│   │   │   ├── Button/
│   │   │   ├── Card/
│   │   │   ├── Input/
│   │   │   ├── Modal/
│   │   │   └── Loading/
│   │   │
│   │   ├── pages/
│   │   │   ├── Home/
│   │   │   ├── Login/
│   │   │   ├── Cadastro/
│   │   │   │
│   │   │   ├── Voluntario/
│   │   │   │   ├── Dashboard/
│   │   │   │   ├── Perfil/
│   │   │   │   ├── Oportunidades/
│   │   │   │   └── Inscricoes/
│   │   │   │
│   │   │   └── Ong/
│   │   │       ├── Dashboard/
│   │   │       ├── Perfil/
│   │   │       ├── Oportunidades/
│   │   │       └── Inscritos/
│   │   │
│   │   ├── services/
│   │   ├── routes/
│   │   ├── context/
│   │   ├── styles/
│   │   ├── App.jsx
│   │   └── main.jsx
│   │
│   ├── package.json
│   └── README.md
│
├── backend/
│   │
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── br/com/conectebem/
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── model/
│   │   │   │       ├── dto/
│   │   │   │       ├── config/
│   │   │   │       ├── security/
│   │   │   │       └── exception/
│   │   │   │
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   │
│   │   └── test/
│   │
│   ├── pom.xml
│   └── README.md
│
├── docs/
│   ├── banco-de-dados/
│   ├── api/
│   ├── testes/
│   └── apresentacao/
│
├── .gitignore
└── README.md