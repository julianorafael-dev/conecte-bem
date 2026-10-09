-- Conecte Bem — Schema oficial do banco
-- Reflete exatamente as entidades JPA em develop (09/10/2026)

DROP TABLE IF EXISTS inscricoes CASCADE;
DROP TABLE IF EXISTS oportunidades CASCADE;
DROP TABLE IF EXISTS ongs CASCADE;
DROP TABLE IF EXISTS categorias CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;


-- Tabela: usuarios
-- ---------------------------------------------------------

-- Corrigido: o enum TipoUsuario.java só tem VOLUNTARIO e ONG
-- (maiúsculas, sem ADMIN). O rascunho antigo usava minúsculas
-- e um valor 'admin' que não existe no código — isso quebraria
-- todo POST /auth/registro.

CREATE TABLE usuarios (
    id          SERIAL PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    senha       VARCHAR(255) NOT NULL,
    tipo        VARCHAR(20)  NOT NULL CHECK (tipo IN ('VOLUNTARIO', 'ONG')),
    criado_em   TIMESTAMP
);

-- Tabela: ongs
-- ---------------------------------------------------------
-- UNIQUE(usuario_id): decisão documentada no README do módulo ONG
-- (relação 1:1 — cada usuário tipo ONG só pode ter um cadastro de ONG).
-- A FK é manual aqui porque Ong.java usa um Integer simples (usuarioId),
-- não um @ManyToOne — então o Hibernate não cria essa FK sozinho.
CREATE TABLE ongs (
    id          SERIAL PRIMARY KEY,
    usuario_id  INTEGER NOT NULL,
    nome        VARCHAR(150) NOT NULL,
    cnpj        VARCHAR(18) NOT NULL UNIQUE,
    descricao   TEXT,
    telefone    VARCHAR(20),
    cidade      VARCHAR(100),
    estado      CHAR(2),
    CONSTRAINT fk_ongs_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_ongs_usuario
        UNIQUE (usuario_id)
);


-- Tabela: categorias
-- ---------------------------------------------------------
CREATE TABLE categorias (
    id          SERIAL PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL UNIQUE,
    descricao   TEXT NOT NULL
);

-- Tabela: oportunidades
-- ---------------------------------------------------------
-- data, horario e criado_em são VARCHAR (não DATE/TIME/TIMESTAMP):
-- Oportunidade.java usa String nesses três campos, não tipos de data.
-- status também é VARCHAR livre, sem CHECK: OportunidadeRequestDTO
-- aceita qualquer texto não-vazio (@NotBlank), sem restringir valores.
-- Convenção esperada pelo time: 'aberta', 'encerrada', 'cancelada'.
CREATE TABLE oportunidades (
    id            SERIAL PRIMARY KEY,
    ong_id        INTEGER NOT NULL,
    categoria_id  INTEGER NOT NULL,
    titulo        VARCHAR(200) NOT NULL,
    descricao     TEXT,
    data          VARCHAR(50) NOT NULL,
    horario       VARCHAR(10) NOT NULL,
    cidade        VARCHAR(100),
    estado        CHAR(2),
    vagas         INTEGER NOT NULL CHECK (vagas >= 0),
    status        VARCHAR(20),
    criado_em     VARCHAR(255) NOT NULL,
    CONSTRAINT fk_oportunidades_ong
        FOREIGN KEY (ong_id) REFERENCES ongs(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_oportunidades_categoria
        FOREIGN KEY (categoria_id) REFERENCES categorias(id)
        ON DELETE RESTRICT
);

-- Tabela: inscricoes
-- ---------------------------------------------------------
-- Ainda não implementada no código (sem model/repository/controller).
-- Mantida aqui para documentar o design e já deixar o schema pronto
-- para quando o módulo for iniciado.
CREATE TABLE inscricoes (
    id               SERIAL PRIMARY KEY,
    usuario_id       INTEGER NOT NULL,
    oportunidade_id  INTEGER NOT NULL,
    data_inscricao   TIMESTAMP NOT NULL DEFAULT NOW(),
    status           VARCHAR(20) NOT NULL DEFAULT 'pendente'
                     CHECK (status IN ('pendente', 'confirmada', 'cancelada')),
    CONSTRAINT fk_inscricoes_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_inscricoes_oportunidade
        FOREIGN KEY (oportunidade_id) REFERENCES oportunidades(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_inscricao_usuario_oportunidade
        UNIQUE (usuario_id, oportunidade_id)
);

-- Índices auxiliares
-- ---------------------------------------------------------
CREATE INDEX idx_oportunidades_cidade_estado ON oportunidades(cidade, estado);
CREATE INDEX idx_oportunidades_status ON oportunidades(status);
CREATE INDEX idx_inscricoes_status ON inscricoes(status);