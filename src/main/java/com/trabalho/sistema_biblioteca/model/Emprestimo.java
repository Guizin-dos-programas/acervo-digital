package com.trabalho.sistema_biblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "emprestimos")
@Getter @Setter @NoArgsConstructor
public class Emprestimo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Column(name = "data_emprestimo", nullable = false)
    private LocalDate dataEmprestimo;

    @Column(name = "data_devolucao_prevista", nullable = false)
    private LocalDate dataDevolucaoPrevista;

    @Column(name = "data_devolucao")
    private LocalDate dataDevolucao;

    // Valores compatíveis com o CHECK existente na migração V1.
    @Column(nullable = false, length = 20)
    private String status = "emprestado";

    @Column(name = "valor_multa", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMulta = BigDecimal.ZERO;

    @Column(name = "renovacoes_realizadas", nullable = false)
    private Integer renovacoesRealizadas = 0;

    @Column(name = "multa_quitada", nullable = false)
    private Boolean multaQuitada = false;
}
