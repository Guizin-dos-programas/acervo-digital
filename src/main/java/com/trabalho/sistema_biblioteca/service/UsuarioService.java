package com.trabalho.sistema_biblioteca.service;

import com.trabalho.sistema_biblioteca.dto.request.UsuarioRequestDto;
import com.trabalho.sistema_biblioteca.dto.response.UsuarioResponseDto;
import com.trabalho.sistema_biblioteca.exception.EntidadeJaCadastradaException;
import com.trabalho.sistema_biblioteca.exception.EntidadeNaoEncontradaException;
import com.trabalho.sistema_biblioteca.exception.RegraNegocioException;
import com.trabalho.sistema_biblioteca.model.Usuario;
import com.trabalho.sistema_biblioteca.model.enums.TipoUsuario;
import com.trabalho.sistema_biblioteca.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhaEncoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder senhaEncoder) {
        this.usuarios = usuarios;
        this.senhaEncoder = senhaEncoder;
    }

    @Transactional
    public UsuarioResponseDto cadastrarUsuario(UsuarioRequestDto req) {
        if (req.tipo() == null || req.tipo() == TipoUsuario.BIBLIOTECARIO) {
            throw new RegraNegocioException("Cadastro público permitido somente para ALUNO ou PROFESSOR");
        }
        if (req.senha() == null || req.senha().isBlank()) {
            throw new RegraNegocioException("A senha é obrigatória");
        }
        if (usuarios.findByCpf(req.cpf()).isPresent()) {
            throw new EntidadeJaCadastradaException("CPF já cadastrado");
        }
        if (usuarios.findByEmail(req.email()).isPresent()) {
            throw new EntidadeJaCadastradaException("Email já cadastrado");
        }
        Usuario novo = new Usuario(req.nome(), req.cpf(), req.email(), req.tipo(), senhaEncoder.encode(req.senha()));
        return new UsuarioResponseDto(usuarios.save(novo));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> listarTodos() {
        return usuarios.findAll().stream().map(UsuarioResponseDto::new).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDto buscarPorId(Long id) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado"));
        return new UsuarioResponseDto(usuario);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado"));
        usuario.desativar();
    }
}
