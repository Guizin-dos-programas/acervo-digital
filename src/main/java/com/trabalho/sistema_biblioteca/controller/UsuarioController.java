package com.trabalho.sistema_biblioteca.controller;

import com.trabalho.sistema_biblioteca.dto.request.UsuarioRequestDto;
import com.trabalho.sistema_biblioteca.dto.response.UsuarioResponseDto;
import com.trabalho.sistema_biblioteca.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDto cadastrar(@Valid @RequestBody UsuarioRequestDto req) {
        return service.cadastrarUsuario(req);
    }
}
