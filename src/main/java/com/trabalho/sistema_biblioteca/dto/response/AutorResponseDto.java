package com.trabalho.sistema_biblioteca.dto.response;
import com.trabalho.sistema_biblioteca.model.Autor;
public record AutorResponseDto(Long id, String nome) {
    public AutorResponseDto(Autor a) { this(a.getId(), a.getNome()); }
}
