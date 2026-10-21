package com.trabalho.sistema_biblioteca.controller;

import com.trabalho.sistema_biblioteca.dto.request.CategoriaRequestDto;
import com.trabalho.sistema_biblioteca.dto.response.CategoriaResponseDto;
import com.trabalho.sistema_biblioteca.service.AcervoAuxiliarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final AcervoAuxiliarService service;
    public CategoriaController(AcervoAuxiliarService service) { this.service = service; }
    @GetMapping public List<CategoriaResponseDto> listar() { return service.listarCategorias(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponseDto criar(@Valid @RequestBody CategoriaRequestDto req) {
        return service.cadastrarCategoria(req);
    }
}
