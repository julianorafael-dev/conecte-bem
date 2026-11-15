-- Executar no banco existente antes de iniciar o backend desta versão.
-- Campos anuláveis preservam contas antigas e cadastros via Google.
BEGIN;
ALTER TABLE public.usuarios ADD COLUMN IF NOT EXISTS cpf varchar(11);
ALTER TABLE public.usuarios ADD COLUMN IF NOT EXISTS data_nascimento date;
ALTER TABLE public.usuarios ADD COLUMN IF NOT EXISTS telefone varchar(20);
ALTER TABLE public.ongs ADD COLUMN IF NOT EXISTS data_criacao date;
COMMIT;
