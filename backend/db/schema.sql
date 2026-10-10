--
-- PostgreSQL database dump
--

\restrict B9rXFhcoyabkHUXMECjCb30wcUNUrGCXyLmLkShlRfpWBcmMDgIDcD8VCuCVZWE



-- Started on 2026-10-09 21:15:15

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 222 (class 1259 OID 16686)
-- Name: categorias; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.categorias (
    id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text
);


--
-- TOC entry 221 (class 1259 OID 16685)
-- Name: categorias_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.categorias_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4855 (class 0 OID 0)
-- Dependencies: 221
-- Name: categorias_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.categorias_id_seq OWNED BY public.categorias.id;


--
-- TOC entry 226 (class 1259 OID 16720)
-- Name: inscricoes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inscricoes (
    id integer NOT NULL,
    usuario_id integer NOT NULL,
    oportunidade_id integer NOT NULL,
    data_inscricao timestamp without time zone DEFAULT now() NOT NULL,
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    CONSTRAINT inscricoes_status_check CHECK (((status)::text = ANY ((ARRAY['PENDENTE'::character varying, 'CONFIRMADA'::character varying, 'CANCELADA'::character varying])::text[])))
);


--
-- TOC entry 225 (class 1259 OID 16719)
-- Name: inscricoes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inscricoes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4856 (class 0 OID 0)
-- Dependencies: 225
-- Name: inscricoes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.inscricoes_id_seq OWNED BY public.inscricoes.id;


--
-- TOC entry 220 (class 1259 OID 16668)
-- Name: ongs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ongs (
    id integer NOT NULL,
    usuario_id integer NOT NULL,
    nome character varying(150) NOT NULL,
    cnpj character varying(18) NOT NULL,
    descricao text,
    telefone character varying(20),
    cidade character varying(100),
    estado character varying(2)
);


--
-- TOC entry 219 (class 1259 OID 16667)
-- Name: ongs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ongs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4857 (class 0 OID 0)
-- Dependencies: 219
-- Name: ongs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ongs_id_seq OWNED BY public.ongs.id;


--
-- TOC entry 224 (class 1259 OID 16697)
-- Name: oportunidades; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oportunidades (
    id integer NOT NULL,
    ong_id integer NOT NULL,
    categoria_id bigint NOT NULL,
    titulo character varying(150) NOT NULL,
    descricao text,
    data date NOT NULL,
    horario time without time zone,
    cidade character varying(100),
    estado character(2),
    vagas integer NOT NULL,
    status character varying(20) DEFAULT 'ABERTA'::character varying NOT NULL,
    criado_em timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT oportunidades_status_check CHECK (((status)::text = ANY ((ARRAY['ABERTA'::character varying, 'ENCERRADA'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT oportunidades_vagas_check CHECK ((vagas >= 0))
);


--
-- TOC entry 223 (class 1259 OID 16696)
-- Name: oportunidades_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oportunidades_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4858 (class 0 OID 0)
-- Dependencies: 223
-- Name: oportunidades_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oportunidades_id_seq OWNED BY public.oportunidades.id;


--
-- TOC entry 218 (class 1259 OID 16655)
-- Name: usuarios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuarios (
    id integer NOT NULL,
    nome character varying(150) NOT NULL,
    email character varying(150) NOT NULL,
    senha character varying(255) NOT NULL,
    tipo character varying(20) NOT NULL,
    criado_em timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT usuarios_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['VOLUNTARIO'::character varying, 'ONG'::character varying, 'ADMIN'::character varying])::text[])))
);


--
-- TOC entry 217 (class 1259 OID 16654)
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.usuarios_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4859 (class 0 OID 0)
-- Dependencies: 217
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- TOC entry 4664 (class 2604 OID 16789)
-- Name: categorias id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categorias ALTER COLUMN id SET DEFAULT nextval('public.categorias_id_seq'::regclass);


--
-- TOC entry 4668 (class 2604 OID 16723)
-- Name: inscricoes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inscricoes ALTER COLUMN id SET DEFAULT nextval('public.inscricoes_id_seq'::regclass);


--
-- TOC entry 4663 (class 2604 OID 16671)
-- Name: ongs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ongs ALTER COLUMN id SET DEFAULT nextval('public.ongs_id_seq'::regclass);


--
-- TOC entry 4665 (class 2604 OID 16700)
-- Name: oportunidades id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oportunidades ALTER COLUMN id SET DEFAULT nextval('public.oportunidades_id_seq'::regclass);


--
-- TOC entry 4661 (class 2604 OID 16658)
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- TOC entry 4688 (class 2606 OID 16695)
-- Name: categorias categorias_nome_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categorias
    ADD CONSTRAINT categorias_nome_key UNIQUE (nome);


--
-- TOC entry 4690 (class 2606 OID 16791)
-- Name: categorias categorias_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categorias
    ADD CONSTRAINT categorias_pkey PRIMARY KEY (id);


--
-- TOC entry 4697 (class 2606 OID 16728)
-- Name: inscricoes inscricoes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inscricoes
    ADD CONSTRAINT inscricoes_pkey PRIMARY KEY (id);


--
-- TOC entry 4680 (class 2606 OID 16679)
-- Name: ongs ongs_cnpj_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ongs
    ADD CONSTRAINT ongs_cnpj_key UNIQUE (cnpj);


--
-- TOC entry 4682 (class 2606 OID 16675)
-- Name: ongs ongs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ongs
    ADD CONSTRAINT ongs_pkey PRIMARY KEY (id);


--
-- TOC entry 4684 (class 2606 OID 16677)
-- Name: ongs ongs_usuario_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ongs
    ADD CONSTRAINT ongs_usuario_id_key UNIQUE (usuario_id);


--
-- TOC entry 4694 (class 2606 OID 16708)
-- Name: oportunidades oportunidades_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oportunidades
    ADD CONSTRAINT oportunidades_pkey PRIMARY KEY (id);


--
-- TOC entry 4699 (class 2606 OID 16730)
-- Name: inscricoes uq_inscricao_usuario_oportunidade; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inscricoes
    ADD CONSTRAINT uq_inscricao_usuario_oportunidade UNIQUE (usuario_id, oportunidade_id);


--
-- TOC entry 4686 (class 2606 OID 16751)
-- Name: ongs uq_ongs_usuario_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ongs
    ADD CONSTRAINT uq_ongs_usuario_id UNIQUE (usuario_id);


--
-- TOC entry 4676 (class 2606 OID 16666)
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- TOC entry 4678 (class 2606 OID 16664)
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- TOC entry 4695 (class 1259 OID 16743)
-- Name: idx_inscricoes_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_inscricoes_status ON public.inscricoes USING btree (status);


--
-- TOC entry 4691 (class 1259 OID 16741)
-- Name: idx_oportunidades_cidade_estado; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_oportunidades_cidade_estado ON public.oportunidades USING btree (cidade, estado);


--
-- TOC entry 4692 (class 1259 OID 16742)
-- Name: idx_oportunidades_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_oportunidades_status ON public.oportunidades USING btree (status);


--
-- TOC entry 4703 (class 2606 OID 16736)
-- Name: inscricoes fk_inscricoes_oportunidade; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inscricoes
    ADD CONSTRAINT fk_inscricoes_oportunidade FOREIGN KEY (oportunidade_id) REFERENCES public.oportunidades(id) ON DELETE CASCADE;


--
-- TOC entry 4704 (class 2606 OID 16731)
-- Name: inscricoes fk_inscricoes_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inscricoes
    ADD CONSTRAINT fk_inscricoes_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- TOC entry 4700 (class 2606 OID 16680)
-- Name: ongs fk_ongs_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ongs
    ADD CONSTRAINT fk_ongs_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- TOC entry 4701 (class 2606 OID 16792)
-- Name: oportunidades fk_oportunidades_categoria; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oportunidades
    ADD CONSTRAINT fk_oportunidades_categoria FOREIGN KEY (categoria_id) REFERENCES public.categorias(id) ON DELETE RESTRICT;


--
-- TOC entry 4702 (class 2606 OID 16709)
-- Name: oportunidades fk_oportunidades_ong; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oportunidades
    ADD CONSTRAINT fk_oportunidades_ong FOREIGN KEY (ong_id) REFERENCES public.ongs(id) ON DELETE CASCADE;


-- Completed on 2026-10-09 21:15:16

--
-- PostgreSQL database dump complete
--

\unrestrict B9rXFhcoyabkHUXMECjCb30wcUNUrGCXyLmLkShlRfpWBcmMDgIDcD8VCuCVZWE

