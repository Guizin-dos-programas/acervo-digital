package com.trabalho.sistema_biblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDto(@NotBlank @Size(max = 100) String nome) {}
