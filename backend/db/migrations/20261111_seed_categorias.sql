-- Seed de Categorias do Conecte-Bem (QA-017)
-- Executa a inserção inicial ignorando as que já existem.

INSERT INTO public.categorias (nome, descricao) VALUES
                                                    ('Educação e Cultura', 'Apoio a projetos educacionais, aulas de reforço e oficinas culturais.'),
                                                    ('Saúde e Apoio Emocional', 'Ações de suporte a pacientes, campanhas de doação e escuta empática.'),
                                                    ('Meio Ambiente e Animais', 'Preservação ambiental, mutirões e proteção animal.'),
                                                    ('Assistência Social e Comunitária', 'Distribuição de recursos básicos e auxílio em comunidades vulneráveis.'),
                                                    ('Voluntariado Digital', 'Apoio remoto em design, TI, redes sociais e afins.'),
                                                    ('Turismo Social', 'Iniciativas de expedições para desenvolvimento local.')
    ON CONFLICT (nome) DO NOTHING;