package com.trabalho.sistema_biblioteca.service;

import com.trabalho.sistema_biblioteca.dto.request.UsuarioRequestDto;
import com.trabalho.sistema_biblioteca.dto.response.UsuarioResponseDto;
import com.trabalho.sistema_biblioteca.exception.EntidadeJaCadastradaException;
import com.trabalho.sistema_biblioteca.exception.EntidadeNaoEncontradaException;
import com.trabalho.sistema_biblioteca.model.Usuario;
import com.trabalho.sistema_biblioteca.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDto cadastrarUsuario(UsuarioRequestDto usuarioRequestDto){
        if(usuarioRepository.findByCpf(usuarioRequestDto.cpf()).isPresent()) {
            throw new EntidadeJaCadastradaException("Cpf já cadastrado");
        }
        if(usuarioRepository.findByEmail(usuarioRequestDto.email()).isPresent()) {
            throw new EntidadeJaCadastradaException("Email já cadastrado");
        }

        String senha = passwordEncoder.encode(usuarioRequestDto.senha());


        Usuario usuario = new Usuario(usuarioRequestDto.nome(), usuarioRequestDto.cpf(), usuarioRequestDto.email(), usuarioRequestDto.tipo(), senha);
        Usuario salvo = usuarioRepository.save(usuario);

        return new UsuarioResponseDto(salvo);
    }

    public List<UsuarioResponseDto> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponseDto::new)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDto buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado com o ID: " + id));
        return new UsuarioResponseDto(usuario);
    }

    public void desativar(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()-> new EntidadeNaoEncontradaException("Usuario não encontrado"));

        usuario.desativar();
    }
}
