-- ============================================================
-- Schema atualizado (PostgreSQL)
-- Alterações aplicadas:
--   oportunidades.estado    : char(2)      -> varchar(2)
--   oportunidades.criado_em : timestamp    -> timestamp(6)
--   oportunidades.horario   : time         -> time(0)
--   (ongs.estado e oportunidades.data já estavam nesses tipos)
-- ============================================================

CREATE TABLE public.usuarios (
    id        SERIAL PRIMARY KEY,
    nome      varchar(150) NOT NULL,
    email     varchar(150) NOT NULL,
    senha     varchar(255) NOT NULL,
    tipo      varchar(20)  NOT NULL,
    criado_em timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT usuarios_email_key UNIQUE (email),
    CONSTRAINT usuarios_tipo_check
        CHECK (tipo IN ('VOLUNTARIO', 'ONG', 'ADMIN'))
);

CREATE TABLE public.ongs (
    id         SERIAL PRIMARY KEY,
    usuario_id integer      NOT NULL,
    nome       varchar(150) NOT NULL,
    cnpj       varchar(18)  NOT NULL,
    descricao  text,
    telefone   varchar(20),
    cidade     varchar(100),
    estado     varchar(2),
    CONSTRAINT ongs_cnpj_key       UNIQUE (cnpj),
    CONSTRAINT ongs_usuario_id_key UNIQUE (usuario_id),
    CONSTRAINT uq_ongs_usuario_id  UNIQUE (usuario_id),
    CONSTRAINT fk_ongs_usuario
        FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE
);

CREATE TABLE public.categorias (
    id        BIGSERIAL PRIMARY KEY,
    nome      varchar(100) NOT NULL,
    descricao text,
    CONSTRAINT categorias_nome_key UNIQUE (nome)
);

CREATE TABLE public.oportunidades (
    id           SERIAL PRIMARY KEY,
    ong_id       integer      NOT NULL,
    categoria_id bigint       NOT NULL,
    titulo       varchar(150) NOT NULL,
    descricao    text,
    data         date         NOT NULL,
    horario      time(0) without time zone,
    cidade       varchar(100),
    estado       varchar(2),
    vagas        integer      NOT NULL,
    status       varchar(20)  DEFAULT 'ABERTA' NOT NULL,
    criado_em    timestamp(6) without time zone DEFAULT now() NOT NULL,
    CONSTRAINT oportunidades_status_check
        CHECK (status IN ('ABERTA', 'ENCERRADA', 'CANCELADA')),
    CONSTRAINT oportunidades_vagas_check CHECK (vagas >= 0),
    CONSTRAINT fk_oportunidades_ong
        FOREIGN KEY (ong_id) REFERENCES public.ongs(id) ON DELETE CASCADE,
    CONSTRAINT fk_oportunidades_categoria
        FOREIGN KEY (categoria_id) REFERENCES public.categorias(id) ON DELETE RESTRICT
);

CREATE TABLE public.inscricoes (
    id             SERIAL PRIMARY KEY,
    usuario_id     integer NOT NULL,
    oportunidade_id integer NOT NULL,
    data_inscricao timestamp without time zone DEFAULT now() NOT NULL,
    status         varchar(20) DEFAULT 'PENDENTE' NOT NULL,
    CONSTRAINT inscricoes_status_check
        CHECK (status IN ('PENDENTE', 'CONFIRMADA', 'CANCELADA')),
    CONSTRAINT uq_inscricao_usuario_oportunidade UNIQUE (usuario_id, oportunidade_id),
    CONSTRAINT fk_inscricoes_usuario
        FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_inscricoes_oportunidade
        FOREIGN KEY (oportunidade_id) REFERENCES public.oportunidades(id) ON DELETE CASCADE
);

-- Índices
CREATE INDEX idx_inscricoes_status            ON public.inscricoes    USING btree (status);
CREATE INDEX idx_oportunidades_cidade_estado  ON public.oportunidades USING btree (cidade, estado);
CREATE INDEX idx_oportunidades_status         ON public.oportunidades USING btree (status);