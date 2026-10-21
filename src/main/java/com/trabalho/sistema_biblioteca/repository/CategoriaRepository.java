package com.trabalho.sistema_biblioteca.repository;
import com.trabalho.sistema_biblioteca.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNomeIgnoreCase(String nome);
}
