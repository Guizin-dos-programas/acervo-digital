package com.trabalho.sistema_biblioteca;

import com.jayway.jsonpath.JsonPath;
import com.trabalho.sistema_biblioteca.model.Autor;
import com.trabalho.sistema_biblioteca.model.Categoria;
import com.trabalho.sistema_biblioteca.model.Emprestimo;
import com.trabalho.sistema_biblioteca.model.Livro;
import com.trabalho.sistema_biblioteca.model.Usuario;
import com.trabalho.sistema_biblioteca.model.enums.TipoUsuario;
import com.trabalho.sistema_biblioteca.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:acervo_integracao;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.jpa.open-in-view=false"
})
@AutoConfigureMockMvc
@Transactional // Todos os dados de teste sao revertidos ao terminar cada cenario.
class AcervoApiIntegrationTest {
    private static final String ADMIN = "admin-teste@exemplo.com";
    private static final String ALUNO = "aluno-teste@exemplo.com";
    private static final String PROFESSOR = "professor-teste@exemplo.com";
    private static final String SENHA = "Teste1234";

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired AutorRepository autores;
    @Autowired CategoriaRepository categorias;
    @Autowired LivroRepository livros;
    @Autowired EmprestimoRepository emprestimos;
    @Autowired PasswordEncoder encoder;

    private Long autorId;
    private Long categoriaId;
    private Usuario aluno;

    @BeforeEach
    void criarCenario() {
        usuarios.saveAndFlush(new Usuario("Bibliotecario teste", "11122233344", ADMIN,
                TipoUsuario.BIBLIOTECARIO, encoder.encode(SENHA)));
        aluno = usuarios.saveAndFlush(new Usuario("Aluno teste", "55566677788", ALUNO,
                TipoUsuario.ALUNO, encoder.encode(SENHA)));
        usuarios.saveAndFlush(new Usuario("Professor teste", "99988877766", PROFESSOR,
                TipoUsuario.PROFESSOR, encoder.encode(SENHA)));
        Autor autor = new Autor();
        autor.setNome("Autor de integracao");
        autorId = autores.saveAndFlush(autor).getId();
        Categoria categoria = new Categoria();
        categoria.setNome("Categoria de integracao");
        categoriaId = categorias.saveAndFlush(categoria).getId();
    }

    @Test
    void crudLivrosConsultaPublicaEPermissoes() throws Exception {
        mvc.perform(get("/api/livros")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
                .content(novoLivro("Livro protegido", 2))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/livros").with(httpBasic(ALUNO, SENHA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(novoLivro("Livro protegido", 2))).andExpect(status().isForbidden());

        long id = cadastrarLivro("Java na biblioteca", 3);
        mvc.perform(get("/api/livros/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(3))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(3));
        mvc.perform(get("/api/livros").param("titulo", "Java"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/livros").param("autor", "Autor de integracao"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/livros").param("isbn", ""))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));

        mvc.perform(put("/api/livros/{id}", id).with(httpBasic(ADMIN, SENHA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(novoLivro("Java atualizado", 5)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeDisponivel").value(5));
        mvc.perform(delete("/api/livros/{id}", id).with(httpBasic(ADMIN, SENHA)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/livros/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void cadastroAutorCategoriaEValidacoesDeSeguranca() throws Exception {
        mvc.perform(post("/api/autores").with(httpBasic(ADMIN, SENHA))
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Nova autora\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("Nova autora"));
        mvc.perform(post("/api/categorias").with(httpBasic(ADMIN, SENHA))
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Poesia\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Invasor\",\"cpf\":\"00011122233\",\"email\":\"invasor@exemplo.com\",\"tipo\":\"BIBLIOTECARIO\",\"senha\":\"Teste1234\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void alunoEmprestaDevolveEConsultaHistorico() throws Exception {
        long livroId = cadastrarLivro("Emprestimos", 2);
        long emprestimoId = emprestar(ALUNO, livroId, 7);
        mvc.perform(get("/api/livros/{id}", livroId))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(1));
        mvc.perform(get("/api/emprestimos/me").with(httpBasic(ALUNO, SENHA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(emprestimoId));
        mvc.perform(get("/api/emprestimos/usuarios/{id}", aluno.getId()).with(httpBasic(ALUNO, SENHA)))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/emprestimos/{id}/devolver", emprestimoId).with(httpBasic(ADMIN, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("devolvido"))
                .andExpect(jsonPath("$.valorMulta").value(0));
        mvc.perform(get("/api/livros/{id}", livroId))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(2));
        mvc.perform(post("/api/emprestimos/{id}/devolver", emprestimoId).with(httpBasic(ADMIN, SENHA)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void professorRenovaAteOLimite() throws Exception {
        long livroId = cadastrarLivro("Livro do professor", 2);
        long id = emprestar(PROFESSOR, livroId, 14);
        mvc.perform(post("/api/emprestimos/{id}/renovar", id).with(httpBasic(ALUNO, SENHA)))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/emprestimos/{id}/renovar", id).with(httpBasic(PROFESSOR, SENHA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.renovacoesRealizadas").value(1));
        mvc.perform(post("/api/emprestimos/{id}/renovar", id).with(httpBasic(PROFESSOR, SENHA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.renovacoesRealizadas").value(2));
        mvc.perform(post("/api/emprestimos/{id}/renovar", id).with(httpBasic(PROFESSOR, SENHA)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atrasoGeraMultaBloqueiaNovoEmprestimoAteQuitacao() throws Exception {
        long livroId = cadastrarLivro("Atraso e multa", 2);
        Livro livro = livros.findById(livroId).orElseThrow();
        livro.setQuantidadeDisponivel(1);
        Emprestimo antigo = new Emprestimo();
        antigo.setUsuario(aluno);
        antigo.setLivro(livro);
        antigo.setDataEmprestimo(LocalDate.now().minusDays(10));
        antigo.setDataDevolucaoPrevista(LocalDate.now().minusDays(3));
        antigo.setStatus("emprestado");
        antigo.setValorMulta(BigDecimal.ZERO);
        antigo.setMultaQuitada(true);
        antigo.setRenovacoesRealizadas(0);
        long id = emprestimos.saveAndFlush(antigo).getId();

        mvc.perform(post("/api/emprestimos/livros/{id}", livroId).with(httpBasic(ALUNO, SENHA)))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/emprestimos/{id}/devolver", id).with(httpBasic(ADMIN, SENHA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valorMulta").value(6.0))
                .andExpect(jsonPath("$.multaQuitada").value(false));
        mvc.perform(post("/api/emprestimos/livros/{id}", livroId).with(httpBasic(ALUNO, SENHA)))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/emprestimos/{id}/quitar-multa", id).with(httpBasic(ADMIN, SENHA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.multaQuitada").value(true));
        emprestar(ALUNO, livroId, 7);
    }


    @Test
    void estoqueZeradoBloqueiaEmprestimo() throws Exception {
        long livroId = cadastrarLivro("Sem exemplares", 0);
        mvc.perform(post("/api/emprestimos/livros/{id}", livroId).with(httpBasic(ALUNO, SENHA)))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/livros/{id}", livroId))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(0));
    }

    @Test
    void livroComEmprestimoHistoricoNaoPodeSerExcluido() throws Exception {
        long livroId = cadastrarLivro("Preservar historico", 2);
        long emprestimoId = emprestar(ALUNO, livroId, 7);
        mvc.perform(post("/api/emprestimos/{id}/devolver", emprestimoId).with(httpBasic(ADMIN, SENHA)))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/livros/{id}", livroId).with(httpBasic(ADMIN, SENHA)))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/livros/{id}", livroId))
                .andExpect(status().isOk());
    }

    @Test
    void contaBloqueadaNaoAutentica() throws Exception {
        aluno.setStatus(com.trabalho.sistema_biblioteca.model.enums.Status.BLOQUEADO);
        usuarios.saveAndFlush(aluno);
        mvc.perform(get("/api/emprestimos/me").with(httpBasic(ALUNO, SENHA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void isbnDuplicadoRetornaConflito() throws Exception {
        String corpo = String.format(java.util.Locale.ROOT,
            "{\"titulo\":\"Duplicado\",\"isbn\":\"9781234567890\",\"quantidade\":2,\"autorId\":%d,\"categoriaId\":%d}",
            autorId, categoriaId);
        mvc.perform(post("/api/livros").with(httpBasic(ADMIN, SENHA))
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/livros").with(httpBasic(ADMIN, SENHA))
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());
    }

    private long cadastrarLivro(String titulo, int quantidade) throws Exception {
        String resposta = mvc.perform(post("/api/livros").with(httpBasic(ADMIN, SENHA))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(novoLivro(titulo, quantidade)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private long emprestar(String email, long livroId, int prazo) throws Exception {
        String resposta = mvc.perform(post("/api/emprestimos/livros/{id}", livroId)
                    .with(httpBasic(email, SENHA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataDevolucaoPrevista")
                    .value(LocalDate.now().plusDays(prazo).toString()))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private String novoLivro(String titulo, int quantidade) {
        return String.format(java.util.Locale.ROOT,
                "{\"titulo\":\"%s\",\"isbn\":null,\"quantidade\":%d,\"autorId\":%d,\"categoriaId\":%d}",
                titulo, quantidade, autorId, categoriaId);
    }
}
