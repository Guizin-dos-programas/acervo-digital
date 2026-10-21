package com.trabalho.sistema_biblioteca.controller;

import com.trabalho.sistema_biblioteca.dto.request.AutorRequestDto;
import com.trabalho.sistema_biblioteca.dto.response.AutorResponseDto;
import com.trabalho.sistema_biblioteca.service.AcervoAuxiliarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/autores")
public class AutorController {
    private final AcervoAuxiliarService service;
    public AutorController(AcervoAuxiliarService service) { this.service = service; }
    @GetMapping public List<AutorResponseDto> listar() { return service.listarAutores(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AutorResponseDto criar(@Valid @RequestBody AutorRequestDto req) {
        return service.cadastrarAutor(req);
    }
}
