package com.trabalho.sistema_biblioteca.model.enums;

public enum TipoUsuario {
    ALUNO(7),
    PROFESSOR(14),
    BIBLIOTECARIO(0);

    private final int diasPrazoEmprestimo;

    TipoUsuario(int diasPrazoEmprestimo) {
        this.diasPrazoEmprestimo = diasPrazoEmprestimo;
    }

    public int getDiasPrazoEmprestimo() {
        return diasPrazoEmprestimo;
    }
}
