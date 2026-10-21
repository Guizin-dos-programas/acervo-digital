package com.trabalho.sistema_biblioteca.dto.response;

import com.trabalho.sistema_biblioteca.model.Usuario;
import com.trabalho.sistema_biblioteca.model.enums.TipoUsuario;

public record UsuarioResponseDto(

        Long id,
        String nome,
        String cpf,
        String email,
        TipoUsuario tipo,
        String status

) {

    public UsuarioResponseDto(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getEmail(),
                usuario.getTipo(),
                usuario.getStatus()
        );
    }
}
