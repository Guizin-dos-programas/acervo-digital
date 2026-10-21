package com.trabalho.sistema_biblioteca.dto.response;

import com.trabalho.sistema_biblioteca.model.Livro;

public record LivroResponseDto(
        Long id, String titulo, String isbn,
        Integer quantidade, Integer quantidadeDisponivel,
        Long autorId, String autor,
        Long categoriaId, String categoria
) {
    public LivroResponseDto(Livro livro) {
        this(livro.getId(), livro.getTitulo(), livro.getIsbn(),
                livro.getQuantidade(), livro.getQuantidadeDisponivel(),
                livro.getAutor() == null ? null : livro.getAutor().getId(),
                livro.getAutor() == null ? null : livro.getAutor().getNome(),
                livro.getCategoria() == null ? null : livro.getCategoria().getId(),
                livro.getCategoria() == null ? null : livro.getCategoria().getNome());
    }
}
