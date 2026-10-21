package com.trabalho.sistema_biblioteca.service;

import com.trabalho.sistema_biblioteca.dto.request.LivroRequestDto;
import com.trabalho.sistema_biblioteca.exception.RegraNegocioException;
import com.trabalho.sistema_biblioteca.model.Autor;
import com.trabalho.sistema_biblioteca.model.Categoria;
import com.trabalho.sistema_biblioteca.model.Livro;
import com.trabalho.sistema_biblioteca.repository.AutorRepository;
import com.trabalho.sistema_biblioteca.repository.CategoriaRepository;
import com.trabalho.sistema_biblioteca.repository.EmprestimoRepository;
import com.trabalho.sistema_biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {
    @Mock LivroRepository livros;
    @Mock AutorRepository autores;
    @Mock CategoriaRepository categorias;
    @Mock EmprestimoRepository emprestimos;
    @InjectMocks LivroService service;

    @Test
    void novoLivroIniciaComEstoqueDisponivelIgualAoTotal() {
        Autor autor = new Autor(); autor.setId(1L); autor.setNome("Machado de Assis");
        Categoria categoria = new Categoria(); categoria.setId(2L); categoria.setNome("Romance");
        when(autores.findById(1L)).thenReturn(Optional.of(autor));
        when(categorias.findById(2L)).thenReturn(Optional.of(categoria));
        when(livros.save(any(Livro.class))).thenAnswer(inv -> {
            Livro livro = inv.getArgument(0);
            livro.setId(3L);
            return livro;
        });
        var resultado = service.criar(new LivroRequestDto("Dom Casmurro", "9780000000001", 4, 1L, 2L));
        assertEquals(4, resultado.quantidade());
        assertEquals(4, resultado.quantidadeDisponivel());
    }

    @Test
    void consultaSemFiltrosNaoEnviaParametrosNulosParaPostgreSQL() {
        var pagina = org.springframework.data.domain.PageRequest.of(0, 20);
        when(livros.buscar("", "", "", pagina))
                .thenReturn(org.springframework.data.domain.Page.empty(pagina));

        assertTrue(service.listar(null, null, null, pagina).isEmpty());
        verify(livros).buscar("", "", "", pagina);
    }

    @Test
    void consultaNormalizaFiltrosInformados() {
        var pagina = org.springframework.data.domain.PageRequest.of(0, 20);
        when(livros.buscar("Java", "Autor", "123", pagina))
                .thenReturn(org.springframework.data.domain.Page.empty(pagina));

        assertTrue(service.listar(" Java ", " Autor ", " 123 ", pagina).isEmpty());
        verify(livros).buscar("Java", "Autor", "123", pagina);
    }

    @Test
    void naoPermiteReduzirQuantidadeAbaixoDoTotalEmprestado() {
        Livro livro = new Livro();
        livro.setId(10L);
        livro.setQuantidade(5);
        livro.setQuantidadeDisponivel(2); // três exemplares emprestados
        when(livros.findByIdParaAtualizar(10L)).thenReturn(Optional.of(livro));
        var req = new LivroRequestDto("Teste", "123", 2, 1L, 1L);
        assertThrows(RegraNegocioException.class, () -> service.atualizar(10L, req));
    }
}
