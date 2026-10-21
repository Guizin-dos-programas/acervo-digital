package com.trabalho.sistema_biblioteca.service;

import com.trabalho.sistema_biblioteca.dto.request.AutorRequestDto;
import com.trabalho.sistema_biblioteca.dto.request.CategoriaRequestDto;
import com.trabalho.sistema_biblioteca.dto.response.AutorResponseDto;
import com.trabalho.sistema_biblioteca.dto.response.CategoriaResponseDto;
import com.trabalho.sistema_biblioteca.exception.EntidadeJaCadastradaException;
import com.trabalho.sistema_biblioteca.model.Autor;
import com.trabalho.sistema_biblioteca.model.Categoria;
import com.trabalho.sistema_biblioteca.repository.AutorRepository;
import com.trabalho.sistema_biblioteca.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AcervoAuxiliarService {
    private final AutorRepository autores;
    private final CategoriaRepository categorias;

    public AcervoAuxiliarService(AutorRepository autores, CategoriaRepository categorias) {
        this.autores = autores;
        this.categorias = categorias;
    }

    @Transactional
    public AutorResponseDto cadastrarAutor(AutorRequestDto req) {
        Autor autor = new Autor();
        autor.setNome(req.nome().trim());
        return new AutorResponseDto(autores.save(autor));
    }

    @Transactional(readOnly = true)
    public List<AutorResponseDto> listarAutores() {
        return autores.findAll().stream().map(AutorResponseDto::new).toList();
    }

    @Transactional
    public CategoriaResponseDto cadastrarCategoria(CategoriaRequestDto req) {
        if (categorias.existsByNomeIgnoreCase(req.nome().trim())) {
            throw new EntidadeJaCadastradaException("Categoria já cadastrada");
        }
        Categoria categoria = new Categoria();
        categoria.setNome(req.nome().trim());
        return new CategoriaResponseDto(categorias.save(categoria));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDto> listarCategorias() {
        return categorias.findAll().stream().map(CategoriaResponseDto::new).toList();
    }
}
