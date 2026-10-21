package com.trabalho.sistema_biblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "livros")
@Getter @Setter @NoArgsConstructor
public class Livro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(nullable = false)
    private Integer quantidade = 0;

    @Column(name = "quantidade_disponivel", nullable = false)
    private Integer quantidadeDisponivel = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "autor_id")
    private Autor autor;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}
