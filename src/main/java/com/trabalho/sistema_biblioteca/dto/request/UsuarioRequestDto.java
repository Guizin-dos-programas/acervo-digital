package com.trabalho.sistema_biblioteca.dto.request;

import com.trabalho.sistema_biblioteca.model.enums.TipoUsuario;
import jakarta.validation.constraints.*;

public record UsuarioRequestDto(
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "O CPF é obrigatório")
        @Size(min = 11, max = 14, message = "O CPF deve ter entre 11 e 14 caracteres") String cpf,
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido") String email,
        @NotNull(message = "O tipo de usuário é obrigatório") TipoUsuario tipo,
        @NotBlank(message = "A senha é obrigatória")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
                 message = "A senha deve ter no mínimo 8 caracteres, uma letra maiúscula e um número")
        String senha
) {}
