package com.trabalho.sistema_biblioteca.model;

import com.trabalho.sistema_biblioteca.model.enums.Status;
import com.trabalho.sistema_biblioteca.model.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ATIVO;

    @Column(length = 255)
    private String senha;

    public Usuario(String nome, String cpf, String email, TipoUsuario tipo, String senha) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.tipo = tipo;
        this.senha = senha;
    }

    public void desativar(){
        this.status = Status.INATIVO;
    }


}
