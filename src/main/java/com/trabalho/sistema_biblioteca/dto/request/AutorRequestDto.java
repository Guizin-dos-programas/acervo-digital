package com.trabalho.sistema_biblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AutorRequestDto(@NotBlank @Size(max = 150) String nome) {}
