package com.trabalho.sistema_biblioteca.service;

import com.trabalho.sistema_biblioteca.exception.RegraNegocioException;
import com.trabalho.sistema_biblioteca.model.Emprestimo;
import com.trabalho.sistema_biblioteca.model.Livro;
import com.trabalho.sistema_biblioteca.model.Usuario;
import com.trabalho.sistema_biblioteca.model.enums.Status;
import com.trabalho.sistema_biblioteca.model.enums.TipoUsuario;
import com.trabalho.sistema_biblioteca.repository.EmprestimoRepository;
import com.trabalho.sistema_biblioteca.repository.LivroRepository;
import com.trabalho.sistema_biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {
    @Mock EmprestimoRepository emprestimos;
    @Mock LivroRepository livros;
    @Mock UsuarioRepository usuarios;
    EmprestimoService service;
    Usuario aluno;
    Livro livro;

    @BeforeEach
    void preparar() {
        service = new EmprestimoService(emprestimos, livros, usuarios, new BigDecimal("2.00"), 2);
        aluno = new Usuario("Ana", "12345678901", "ana@example.com", TipoUsuario.ALUNO, "hash");
        aluno.setId(11L);
        aluno.setStatus(Status.ATIVO);
        livro = new Livro();
        livro.setId(3L);
        livro.setTitulo("O Cortiço");
        livro.setQuantidade(3);
        livro.setQuantidadeDisponivel(2);
    }

    @Test
    void emprestarReduzEstoqueEAplicaSeteDiasParaAluno() {
        when(usuarios.findByEmail("ana@example.com")).thenReturn(Optional.of(aluno));
        when(livros.findByIdParaAtualizar(3L)).thenReturn(Optional.of(livro));
        when(emprestimos.save(any(Emprestimo.class))).thenAnswer(inv -> {
            Emprestimo e = inv.getArgument(0); e.setId(99L); return e;
        });
        var resultado = service.emprestar("ana@example.com", 3L);
        assertEquals(1, livro.getQuantidadeDisponivel());
        assertEquals(LocalDate.now().plusDays(7), resultado.dataDevolucaoPrevista());
    }

    @Test
    void naoPermiteEmprestimoComUsuarioBloqueado() {
        aluno.setStatus(Status.BLOQUEADO);
        when(usuarios.findByEmail("ana@example.com")).thenReturn(Optional.of(aluno));
        assertThrows(RegraNegocioException.class, () -> service.emprestar("ana@example.com", 3L));
        verifyNoInteractions(livros);
    }

    @Test
    void devolverAcrescentaExemplarECalculaTresDiasDeMulta() {
        livro.setQuantidadeDisponivel(1);
        Emprestimo e = new Emprestimo();
        e.setId(42L);
        e.setUsuario(aluno);
        e.setLivro(livro);
        e.setDataEmprestimo(LocalDate.now().minusDays(10));
        e.setDataDevolucaoPrevista(LocalDate.now().minusDays(3));
        when(emprestimos.findByIdParaAtualizar(42L)).thenReturn(Optional.of(e));
        when(livros.findByIdParaAtualizar(3L)).thenReturn(Optional.of(livro));
        var resultado = service.devolver(42L);
        assertEquals(2, livro.getQuantidadeDisponivel());
        assertEquals(LocalDate.now(), resultado.dataDevolucao());
        assertEquals(0, new BigDecimal("6.00").compareTo(resultado.valorMulta()));
        assertFalse(resultado.multaQuitada());
    }
}
