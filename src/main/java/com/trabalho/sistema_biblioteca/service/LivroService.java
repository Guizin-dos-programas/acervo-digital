package com.trabalho.sistema_biblioteca.service;

import com.trabalho.sistema_biblioteca.dto.request.LivroRequestDto;
import com.trabalho.sistema_biblioteca.dto.response.LivroResponseDto;
import com.trabalho.sistema_biblioteca.exception.EntidadeJaCadastradaException;
import com.trabalho.sistema_biblioteca.exception.EntidadeNaoEncontradaException;
import com.trabalho.sistema_biblioteca.exception.RegraNegocioException;
import com.trabalho.sistema_biblioteca.model.Livro;
import com.trabalho.sistema_biblioteca.repository.AutorRepository;
import com.trabalho.sistema_biblioteca.repository.CategoriaRepository;
import com.trabalho.sistema_biblioteca.repository.EmprestimoRepository;
import com.trabalho.sistema_biblioteca.repository.LivroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LivroService {
    private final LivroRepository livros;
    private final AutorRepository autores;
    private final CategoriaRepository categorias;
    private final EmprestimoRepository emprestimos;

    public LivroService(LivroRepository livros, AutorRepository autores,
                        CategoriaRepository categorias, EmprestimoRepository emprestimos) {
        this.livros = livros;
        this.autores = autores;
        this.categorias = categorias;
        this.emprestimos = emprestimos;
    }

    @Transactional
    public LivroResponseDto criar(LivroRequestDto req) {
        String isbn = normalizar(req.isbn());
        if (isbn != null && livros.existsByIsbn(isbn)) {
            throw new EntidadeJaCadastradaException("ISBN já cadastrado");
        }
        Livro livro = new Livro();
        atribuirDados(livro, req, isbn);
        livro.setQuantidade(req.quantidade());
        livro.setQuantidadeDisponivel(req.quantidade());
        return new LivroResponseDto(livros.save(livro));
    }

    @Transactional
    public LivroResponseDto atualizar(Long id, LivroRequestDto req) {
        Livro livro = livros.findByIdParaAtualizar(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Livro não encontrado"));
        String isbn = normalizar(req.isbn());
        if (isbn != null && !isbn.equals(livro.getIsbn()) && livros.existsByIsbn(isbn)) {
            throw new EntidadeJaCadastradaException("ISBN já cadastrado");
        }
        int exemplaresEmprestados = livro.getQuantidade() - livro.getQuantidadeDisponivel();
        if (req.quantidade() < exemplaresEmprestados) {
            throw new RegraNegocioException("O total não pode ser menor que os exemplares emprestados");
        }
        atribuirDados(livro, req, isbn);
        livro.setQuantidade(req.quantidade());
        livro.setQuantidadeDisponivel(req.quantidade() - exemplaresEmprestados);
        return new LivroResponseDto(livro);
    }

    @Transactional(readOnly = true)
    public LivroResponseDto buscar(Long id) {
        return new LivroResponseDto(livros.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Livro não encontrado")));
    }

    @Transactional(readOnly = true)
    public Page<LivroResponseDto> listar(String titulo, String autor, String isbn, Pageable pagina) {
        return livros.buscar(filtroOuVazio(titulo), filtroOuVazio(autor), filtroOuVazio(isbn), pagina)
                .map(LivroResponseDto::new);
    }

    @Transactional
    public void excluir(Long id) {
        Livro livro = livros.findByIdParaAtualizar(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Livro não encontrado"));
        // Preserva histórico de empréstimos e integridade das chaves estrangeiras.
        if (emprestimos.existsByLivro_Id(id)) {
            throw new RegraNegocioException("Livro possui histórico de empréstimos e não pode ser excluído");
        }
        livros.delete(livro);
    }

    private void atribuirDados(Livro livro, LivroRequestDto req, String isbn) {
        livro.setTitulo(req.titulo().trim());
        livro.setIsbn(isbn);
        livro.setAutor(autores.findById(req.autorId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Autor não encontrado")));
        livro.setCategoria(categorias.findById(req.categoriaId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada")));
    }

    private static String filtroOuVazio(String valor) {
        String normalizado = normalizar(valor);
        return normalizado == null ? "" : normalizado;
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
