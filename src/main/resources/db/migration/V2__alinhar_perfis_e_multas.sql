-- Nunca altere a migração V1 já aplicada. Esta V2 harmoniza os enums Java
-- com os dados legados e permite identificar multas ainda não quitadas.
ALTER TABLE public.usuarios DROP CONSTRAINT IF EXISTS usuarios_status_check;
ALTER TABLE public.usuarios DROP CONSTRAINT IF EXISTS usuarios_tipo_check;

UPDATE public.usuarios
SET status = CASE
    WHEN status IN ('Ativo', 'ATIVO') THEN 'ATIVO'
    WHEN status IN ('Bloqueado', 'BLOQUEADO') THEN 'BLOQUEADO'
    WHEN status IN ('Inativo', 'INATIVO') THEN 'INATIVO'
    ELSE status
END;

UPDATE public.usuarios
SET tipo = CASE
    WHEN tipo IN ('Aluno', 'ALUNO') THEN 'ALUNO'
    WHEN tipo IN ('Professor', 'PROFESSOR') THEN 'PROFESSOR'
    WHEN tipo IN ('Bibliotecário', 'Bibliotecario', 'BIBLIOTECARIO') THEN 'BIBLIOTECARIO'
    ELSE tipo
END;

ALTER TABLE public.usuarios ALTER COLUMN status SET DEFAULT 'ATIVO';
ALTER TABLE public.usuarios ADD CONSTRAINT usuarios_status_check
    CHECK (status IN ('ATIVO', 'INATIVO', 'BLOQUEADO'));
ALTER TABLE public.usuarios ADD CONSTRAINT usuarios_tipo_check
    CHECK (tipo IN ('ALUNO', 'PROFESSOR', 'BIBLIOTECARIO'));

ALTER TABLE public.emprestimos
    ADD COLUMN IF NOT EXISTS multa_quitada BOOLEAN NOT NULL DEFAULT FALSE;
-- Dados antigos com multa zero não representam dívida pendente.
UPDATE public.emprestimos SET multa_quitada = TRUE WHERE valor_multa = 0;
