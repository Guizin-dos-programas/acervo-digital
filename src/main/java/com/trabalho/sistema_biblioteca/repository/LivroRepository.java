package com.trabalho.sistema_biblioteca.repository;

import com.trabalho.sistema_biblioteca.model.Livro;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {
    boolean existsByIsbn(String isbn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Livro l WHERE l.id = :id")
    Optional<Livro> findByIdParaAtualizar(@Param("id") Long id);

    @Query("""
            SELECT l FROM Livro l
            LEFT JOIN l.autor a
            WHERE (:titulo = '' OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
              AND (:autor = '' OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :autor, '%')))
              AND (:isbn = '' OR l.isbn LIKE CONCAT('%', :isbn, '%'))
            """)
    Page<Livro> buscar(@Param("titulo") String titulo, @Param("autor") String autor,
                       @Param("isbn") String isbn, Pageable pageable);
}
