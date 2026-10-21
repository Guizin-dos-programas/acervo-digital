package com.trabalho.sistema_biblioteca.dto.request;

import jakarta.validation.constraints.*;

public record LivroRequestDto(
        @NotBlank @Size(max = 200) String titulo,
        @Size(max = 20) String isbn,
        @NotNull @Min(0) Integer quantidade,
        @NotNull Long autorId,
        @NotNull Long categoriaId
) {}
