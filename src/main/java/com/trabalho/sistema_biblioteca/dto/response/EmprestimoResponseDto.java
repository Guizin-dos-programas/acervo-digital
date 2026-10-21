package com.trabalho.sistema_biblioteca.dto.response;

import com.trabalho.sistema_biblioteca.model.Emprestimo;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmprestimoResponseDto(
        Long id, Long usuarioId, Long livroId, String titulo,
        LocalDate dataEmprestimo, LocalDate dataDevolucaoPrevista,
        LocalDate dataDevolucao, String status, BigDecimal valorMulta,
        Integer renovacoesRealizadas, Boolean multaQuitada
) {
    public EmprestimoResponseDto(Emprestimo e) {
        this(e.getId(), e.getUsuario().getId(), e.getLivro().getId(),
                e.getLivro().getTitulo(), e.getDataEmprestimo(),
                e.getDataDevolucaoPrevista(), e.getDataDevolucao(),
                (e.getDataDevolucao() == null &&
                 e.getDataDevolucaoPrevista().isBefore(LocalDate.now()))
                         ? "atrasado" : e.getStatus(),
                e.getValorMulta(), e.getRenovacoesRealizadas(), e.getMultaQuitada());
    }
}
