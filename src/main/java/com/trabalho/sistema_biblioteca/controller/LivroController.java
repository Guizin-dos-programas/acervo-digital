package com.trabalho.sistema_biblioteca.controller;

import com.trabalho.sistema_biblioteca.dto.request.LivroRequestDto;
import com.trabalho.sistema_biblioteca.dto.response.LivroResponseDto;
import com.trabalho.sistema_biblioteca.service.LivroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/livros")
public class LivroController {
    private final LivroService service;

    public LivroController(LivroService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivroResponseDto criar(@Valid @RequestBody LivroRequestDto req) {
        return service.criar(req);
    }

    @GetMapping("/{id}")
    public LivroResponseDto obter(@PathVariable Long id) { return service.buscar(id); }

    @GetMapping
    public Page<LivroResponseDto> listar(@RequestParam(required = false) String titulo,
                                         @RequestParam(required = false) String autor,
                                         @RequestParam(required = false) String isbn,
                                         @PageableDefault(size = 20) Pageable pagina) {
        return service.listar(titulo, autor, isbn, pagina);
    }

    @PutMapping("/{id}")
    public LivroResponseDto atualizar(@PathVariable Long id, @Valid @RequestBody LivroRequestDto req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) { service.excluir(id); }
}
