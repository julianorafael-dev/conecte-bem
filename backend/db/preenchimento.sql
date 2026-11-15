
--Criar oportunidades, Ongs e Voluntarios
BEGIN;

-- Categorias
INSERT INTO public.categorias (nome, descricao)
SELECT v.nome, v.descricao
FROM (VALUES
    ('Educação e Cultura',
     'Apoio a projetos educacionais, aulas de reforço e oficinas culturais.'),
    ('Saúde e Apoio Emocional',
     'Ações de suporte a pacientes, campanhas de doação e escuta empática.'),
    ('Meio Ambiente e Animais',
     'Preservação ambiental, mutirões e proteção animal.'),
    ('Assistência Social e Comunitária',
     'Distribuição de recursos básicos e auxílio em comunidades vulneráveis.'),
    ('Voluntariado Digital',
     'Apoio remoto em design, TI e redes sociais.'),
    ('Turismo Social',
     'Expedições para desenvolvimento sustentável e apoio comunitário.')
) AS v(nome, descricao)
WHERE NOT EXISTS (
    SELECT 1
    FROM public.categorias c
    WHERE lower(c.nome) = lower(v.nome)
);

-- Contas das ONGs e dos voluntários
INSERT INTO public.usuarios (
    nome, email, senha, tipo, data_nascimento, criado_em
)
SELECT
    v.nome,
    v.email,
    '$2a$10$eSmQa9RGY/kgrQGh3Bdyc.BZJq96M6puQg9QAOLSG8LTTMCht5Gqi',
    v.tipo,
    v.nascimento::date,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('Instituto Novos Caminhos',
     'caminhos@example.com', 'ONG', NULL),
    ('Associação Cuidar e Acolher',
     'acolher@example.com', 'ONG', NULL),
    ('Instituto Raízes Verdes',
     'raizes@example.com', 'ONG', NULL),
    ('Ana Martins',
     'ana@example.com', 'VOLUNTARIO', '1998-03-12'),
    ('Bruno Santos',
     'bruno@example.com', 'VOLUNTARIO', '1995-07-21'),
    ('Carla Oliveira',
     'carla@example.com', 'VOLUNTARIO', '2000-01-18'),
    ('Diego Almeida',
     'diego@example.com', 'VOLUNTARIO', '1992-11-05'),
    ('Elisa Costa',
     'elisa@example.com', 'VOLUNTARIO', '2002-06-24'),
    ('Felipe Ribeiro',
     'felipe@example.com', 'VOLUNTARIO', '1997-09-09')
) AS v(nome, email, tipo, nascimento)
ON CONFLICT (email) DO NOTHING;

-- Perfis das ONGs: dados fictícios para teste
INSERT INTO public.ongs (
    usuario_id, nome, cnpj, descricao,
    data_criacao, cidade, estado
)
SELECT
    u.id,
    v.nome,
    v.cnpj,
    v.descricao,
    DATE '2020-01-10',
    v.cidade,
    v.estado
FROM (VALUES
    (
        'caminhos@example.com',
        'Instituto Novos Caminhos',
        '00000000000191',
        'ONG fictícia dedicada à educação, cultura e inclusão digital.',
        'São Paulo',
        'SP'
    ),
    (
        'acolher@example.com',
        'Associação Cuidar e Acolher',
        '00000000000272',
        'ONG fictícia de apoio social e acolhimento comunitário.',
        'Campinas',
        'SP'
    ),
    (
        'raizes@example.com',
        'Instituto Raízes Verdes',
        '00000000000353',
        'ONG fictícia de preservação ambiental e turismo comunitário.',
        'Curitiba',
        'PR'
    )
) AS v(email, nome, cnpj, descricao, cidade, estado)
JOIN public.usuarios u
    ON u.email = v.email
    AND u.tipo = 'ONG'
ON CONFLICT DO NOTHING;

-- Oportunidades com datas futuras
INSERT INTO public.oportunidades (
    ong_id, categoria_id, titulo, descricao,
    data, horario, cidade, estado, vagas, status, criado_em
)
SELECT
    o.id,
    c.id,
    v.titulo,
    v.descricao,
    (CURRENT_TIMESTAMP AT TIME ZONE 'America/Sao_Paulo')::date + v.dias,
    v.horario::time,
    o.cidade,
    o.estado,
    v.vagas,
    'ABERTA',
    CURRENT_TIMESTAMP
FROM (VALUES
    (
        'caminhos@example.com',
        'Educação e Cultura',
        'Reforço escolar e leitura',
        'Apoie estudantes com atividades de leitura e matemática. Haverá orientação da equipe.',
        7, '09:00', 8
    ),
    (
        'acolher@example.com',
        'Saúde e Apoio Emocional',
        'Acolhimento em campanha de doação de sangue',
        'Receba e oriente doadores, organize filas e distribua materiais informativos, sem atuação clínica.',
        10, '08:00', 12
    ),
    (
        'raizes@example.com',
        'Meio Ambiente e Animais',
        'Mutirão de limpeza e plantio',
        'Participe da coleta de resíduos e do plantio de mudas. A equipe fornecerá materiais e orientação.',
        14, '08:30', 20
    ),
    (
        'acolher@example.com',
        'Assistência Social e Comunitária',
        'Montagem de cestas solidárias',
        'Ajude a separar alimentos, montar cestas e organizar a entrega às famílias atendidas.',
        9, '14:00', 15
    ),
    (
        'caminhos@example.com',
        'Voluntariado Digital',
        'Design e redes sociais para projetos sociais',
        'Crie peças e organize publicações para divulgar ações sociais. Atividade remota com reunião inicial online.',
        12, '19:00', 5
    ),
    (
        'raizes@example.com',
        'Turismo Social',
        'Vivência e apoio ao turismo comunitário',
        'Apoie uma visita guiada com moradores locais e atividades de educação ambiental. Ponto de encontro na sede.',
        21, '07:30', 10
    )
) AS v(email, categoria, titulo, descricao, dias, horario, vagas)
JOIN public.usuarios u
    ON u.email = v.email
JOIN public.ongs o
    ON o.usuario_id = u.id
JOIN public.categorias c
    ON c.id = (
        SELECT min(c2.id)
        FROM public.categorias c2
        WHERE lower(c2.nome) = lower(v.categoria)
    )
WHERE NOT EXISTS (
    SELECT 1
    FROM public.oportunidades existente
    WHERE existente.ong_id = o.id
      AND existente.titulo = v.titulo
);

COMMIT;
