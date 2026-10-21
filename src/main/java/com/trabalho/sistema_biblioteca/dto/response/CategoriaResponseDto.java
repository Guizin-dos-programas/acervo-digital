package com.trabalho.sistema_biblioteca.dto.response;
import com.trabalho.sistema_biblioteca.model.Categoria;
public record CategoriaResponseDto(Long id, String nome) {
    public CategoriaResponseDto(Categoria c) { this(c.getId(), c.getNome()); }
}
