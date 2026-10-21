package com.trabalho.sistema_biblioteca.controller;

import com.trabalho.sistema_biblioteca.dto.response.EmprestimoResponseDto;
import com.trabalho.sistema_biblioteca.service.EmprestimoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/emprestimos")
public class EmprestimoController {
    private final EmprestimoService service;
    public EmprestimoController(EmprestimoService service) { this.service = service; }

    @PostMapping("/livros/{livroId}")
    @ResponseStatus(HttpStatus.CREATED)
    public EmprestimoResponseDto emprestar(@PathVariable Long livroId, Authentication autenticacao) {
        return service.emprestar(autenticacao.getName(), livroId);
    }

    @PostMapping("/{id}/devolver")
    public EmprestimoResponseDto devolver(@PathVariable Long id) {
        return service.devolver(id);
    }

    @PostMapping("/{id}/renovar")
    public EmprestimoResponseDto renovar(@PathVariable Long id, Authentication autenticacao) {
        return service.renovar(autenticacao.getName(), id);
    }

    @PostMapping("/{id}/quitar-multa")
    public EmprestimoResponseDto quitarMulta(@PathVariable Long id) {
        return service.quitarMulta(id);
    }

    @GetMapping("/me")
    public List<EmprestimoResponseDto> meusEmprestimos(Authentication autenticacao) {
        return service.meuHistorico(autenticacao.getName());
    }

    @GetMapping("/usuarios/{usuarioId}")
    public List<EmprestimoResponseDto> historicoDoUsuario(@PathVariable Long usuarioId) {
        return service.historicoPorId(usuarioId);
    }
}
