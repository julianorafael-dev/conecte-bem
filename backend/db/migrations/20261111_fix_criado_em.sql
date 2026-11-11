-- Corrige oportunidades.criado_em (estava varchar; a entidade espera timestamp).
BEGIN;
ALTER TABLE public.oportunidades
    ALTER COLUMN criado_em TYPE timestamp(6) without time zone
    USING criado_em::timestamp(6) without time zone;
ALTER TABLE public.oportunidades ALTER COLUMN criado_em SET DEFAULT now();
ALTER TABLE public.oportunidades ALTER COLUMN criado_em SET NOT NULL;
COMMIT;
