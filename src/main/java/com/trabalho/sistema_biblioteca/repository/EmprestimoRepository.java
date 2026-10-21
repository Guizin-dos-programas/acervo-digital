package com.trabalho.sistema_biblioteca.repository;

import com.trabalho.sistema_biblioteca.model.Emprestimo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Emprestimo e WHERE e.id = :id")
    Optional<Emprestimo> findByIdParaAtualizar(@Param("id") Long id);

    boolean existsByLivro_IdAndDataDevolucaoIsNull(Long livroId);
    boolean existsByLivro_Id(Long livroId);
    boolean existsByUsuario_IdAndDataDevolucaoIsNullAndDataDevolucaoPrevistaBefore(Long usuarioId, LocalDate hoje);
    boolean existsByUsuario_IdAndValorMultaGreaterThanAndMultaQuitadaFalse(Long usuarioId, BigDecimal valor);
    List<Emprestimo> findByUsuario_IdOrderByDataEmprestimoDescIdDesc(Long usuarioId);
}
