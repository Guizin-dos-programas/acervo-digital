package com.trabalho.sistema_biblioteca.service;

import com.trabalho.sistema_biblioteca.dto.response.EmprestimoResponseDto;
import com.trabalho.sistema_biblioteca.exception.EntidadeNaoEncontradaException;
import com.trabalho.sistema_biblioteca.exception.RegraNegocioException;
import com.trabalho.sistema_biblioteca.model.Emprestimo;
import com.trabalho.sistema_biblioteca.model.Livro;
import com.trabalho.sistema_biblioteca.model.Usuario;
import com.trabalho.sistema_biblioteca.model.enums.Status;
import com.trabalho.sistema_biblioteca.repository.EmprestimoRepository;
import com.trabalho.sistema_biblioteca.repository.LivroRepository;
import com.trabalho.sistema_biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EmprestimoService {
    private final EmprestimoRepository emprestimos;
    private final LivroRepository livros;
    private final UsuarioRepository usuarios;
    private final BigDecimal multaDiaria;
    private final int maxRenovacoes;

    public EmprestimoService(EmprestimoRepository emprestimos,
                             LivroRepository livros, UsuarioRepository usuarios,
                             @Value("${biblioteca.emprestimos.multa-diaria:2.00}") BigDecimal multaDiaria,
                             @Value("${biblioteca.emprestimos.max-renovacoes:2}") int maxRenovacoes) {
        if (multaDiaria.signum() < 0 || maxRenovacoes < 0) {
            throw new IllegalArgumentException("Política de multa/renovação inválida");
        }
        this.emprestimos = emprestimos;
        this.livros = livros;
        this.usuarios = usuarios;
        this.multaDiaria = multaDiaria;
        this.maxRenovacoes = maxRenovacoes;
    }

    @Transactional
    public EmprestimoResponseDto emprestar(String emailAutenticado, Long livroId) {
        Usuario usuario = buscarUsuarioPorEmail(emailAutenticado);
        validarElegibilidade(usuario);
        Livro livro = livros.findByIdParaAtualizar(livroId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Livro não encontrado"));
        if (livro.getQuantidadeDisponivel() <= 0) {
            throw new RegraNegocioException("Não há exemplar disponível para empréstimo");
        }
        livro.setQuantidadeDisponivel(livro.getQuantidadeDisponivel() - 1);
        LocalDate hoje = LocalDate.now();
        Emprestimo e = new Emprestimo();
        e.setLivro(livro);
        e.setUsuario(usuario);
        e.setDataEmprestimo(hoje);
        e.setDataDevolucaoPrevista(hoje.plusDays(usuario.getTipo().getDiasPrazoEmprestimo()));
        e.setStatus("emprestado");
        e.setValorMulta(BigDecimal.ZERO);
        e.setMultaQuitada(true); // não há débito na criação
        e.setRenovacoesRealizadas(0);
        return new EmprestimoResponseDto(emprestimos.save(e));
    }

    @Transactional
    public EmprestimoResponseDto devolver(Long emprestimoId) {
        Emprestimo e = buscarEmprestimoParaAtualizar(emprestimoId);
        if (e.getDataDevolucao() != null) {
            throw new RegraNegocioException("Este empréstimo já foi devolvido");
        }
        Livro livro = livros.findByIdParaAtualizar(e.getLivro().getId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Livro do empréstimo não encontrado"));
        LocalDate hoje = LocalDate.now();
        long diasAtraso = Math.max(0, ChronoUnit.DAYS.between(e.getDataDevolucaoPrevista(), hoje));
        BigDecimal multa = multaDiaria.multiply(BigDecimal.valueOf(diasAtraso));
        e.setDataDevolucao(hoje);
        e.setStatus("devolvido");
        e.setValorMulta(multa);
        e.setMultaQuitada(multa.signum() == 0);
        if (livro.getQuantidadeDisponivel() >= livro.getQuantidade()) {
            throw new RegraNegocioException("Estoque inconsistente: devolução excede quantidade total");
        }
        livro.setQuantidadeDisponivel(livro.getQuantidadeDisponivel() + 1);
        return new EmprestimoResponseDto(e);
    }

    @Transactional
    public EmprestimoResponseDto renovar(String emailAutenticado, Long emprestimoId) {
        Emprestimo e = buscarEmprestimoParaAtualizar(emprestimoId);
        Usuario usuario = buscarUsuarioPorEmail(emailAutenticado);
        if (!e.getUsuario().getId().equals(usuario.getId())) {
            throw new RegraNegocioException("Você não pode renovar empréstimo de outro usuário");
        }
        if (e.getDataDevolucao() != null) {
            throw new RegraNegocioException("Empréstimo já devolvido");
        }
        if (LocalDate.now().isAfter(e.getDataDevolucaoPrevista())) {
            throw new RegraNegocioException("Empréstimo atrasado não pode ser renovado");
        }
        validarElegibilidade(usuario);
        if (e.getRenovacoesRealizadas() >= maxRenovacoes) {
            throw new RegraNegocioException("Limite de renovações atingido");
        }
        e.setDataDevolucaoPrevista(e.getDataDevolucaoPrevista().plusDays(
                usuario.getTipo().getDiasPrazoEmprestimo()));
        e.setRenovacoesRealizadas(e.getRenovacoesRealizadas() + 1);
        return new EmprestimoResponseDto(e);
    }

    @Transactional
    public EmprestimoResponseDto quitarMulta(Long emprestimoId) {
        Emprestimo e = buscarEmprestimoParaAtualizar(emprestimoId);
        if (e.getDataDevolucao() == null) {
            throw new RegraNegocioException("É necessário registrar a devolução antes de quitar a multa");
        }
        e.setMultaQuitada(true);
        return new EmprestimoResponseDto(e);
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponseDto> meuHistorico(String emailAutenticado) {
        return historicoPorId(buscarUsuarioPorEmail(emailAutenticado).getId());
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponseDto> historicoPorId(Long usuarioId) {
        if (!usuarios.existsById(usuarioId)) {
            throw new EntidadeNaoEncontradaException("Usuário não encontrado");
        }
        return emprestimos.findByUsuario_IdOrderByDataEmprestimoDescIdDesc(usuarioId)
                .stream().map(EmprestimoResponseDto::new).toList();
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarios.findByEmail(email)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário autenticado não encontrado"));
    }

    private Emprestimo buscarEmprestimoParaAtualizar(Long id) {
        return emprestimos.findByIdParaAtualizar(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Empréstimo não encontrado"));
    }

    private void validarElegibilidade(Usuario usuario) {
        if (usuario.getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("Usuário inativo ou bloqueado");
        }
        if (usuario.getTipo().getDiasPrazoEmprestimo() <= 0) {
            throw new RegraNegocioException("Perfil sem permissão de empréstimo");
        }
        if (emprestimos.existsByUsuario_IdAndDataDevolucaoIsNullAndDataDevolucaoPrevistaBefore(
                usuario.getId(), LocalDate.now())) {
            throw new RegraNegocioException("Usuário possui empréstimo em atraso");
        }
        if (emprestimos.existsByUsuario_IdAndValorMultaGreaterThanAndMultaQuitadaFalse(
                usuario.getId(), BigDecimal.ZERO)) {
            throw new RegraNegocioException("Usuário possui multa pendente");
        }
    }
}
