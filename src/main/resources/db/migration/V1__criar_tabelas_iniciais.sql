CREATE TABLE public.autores (
    id SERIAL PRIMARY KEY,
    nome character varying(150) NOT NULL
);

CREATE TABLE public.categorias (
    id SERIAL PRIMARY KEY,
    nome character varying(100) NOT NULL UNIQUE
);

CREATE TABLE public.usuarios (
    id SERIAL PRIMARY KEY,
    nome character varying(255) NOT NULL,
    cpf character varying(14) NOT NULL UNIQUE,
    email character varying(255) NOT NULL UNIQUE,
    tipo character varying(50) NOT NULL,
    status character varying(50) DEFAULT 'Ativo'::character varying NOT NULL,
    senha character varying(255),
    CONSTRAINT usuarios_status_check CHECK (((status)::text = ANY ((ARRAY['Ativo'::character varying, 'Bloqueado'::character varying])::text[]))),
    CONSTRAINT usuarios_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['Aluno'::character varying, 'Professor'::character varying, 'Bibliotecário'::character varying])::text[])))
);

CREATE TABLE public.livros (
    id SERIAL PRIMARY KEY,
    titulo character varying(200) NOT NULL,
    isbn character varying(20) UNIQUE,
    quantidade integer DEFAULT 0 NOT NULL,
    quantidade_disponivel integer DEFAULT 0 NOT NULL,
    categoria_id integer,
    autor_id integer,
    CONSTRAINT livros_autor_id_fkey FOREIGN KEY (autor_id) REFERENCES public.autores(id),
    CONSTRAINT livros_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES public.categorias(id)
);

CREATE TABLE public.emprestimos (
    id SERIAL PRIMARY KEY,
    usuario_id integer NOT NULL,
    livro_id integer NOT NULL,
    data_emprestimo date DEFAULT CURRENT_DATE NOT NULL,
    data_devolucao_prevista date NOT NULL,
    data_devolucao date,
    status character varying(20) DEFAULT 'emprestado'::character varying NOT NULL,
    valor_multa numeric(10,2) DEFAULT 0.00,
    renovacoes_realizadas integer DEFAULT 0 NOT NULL,
    CONSTRAINT emprestimos_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id),
    CONSTRAINT emprestimos_livro_id_fkey FOREIGN KEY (livro_id) REFERENCES public.livros(id),
    CONSTRAINT emprestimos_status_check CHECK (((status)::text = ANY ((ARRAY['emprestado'::character varying, 'devolvido'::character varying, 'atrasado'::character varying])::text[])))
);