package com.trabalho.sistema_biblioteca.config;

import com.trabalho.sistema_biblioteca.exception.EntidadeJaCadastradaException;
import com.trabalho.sistema_biblioteca.exception.EntidadeNaoEncontradaException;
import com.trabalho.sistema_biblioteca.exception.RegraNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    ResponseEntity<Map<String, String>> naoEncontrada(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler({EntidadeJaCadastradaException.class, DataIntegrityViolationException.class})
    ResponseEntity<Map<String, String>> conflito(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", "Registro duplicado ou relacionado a outros registros"));
    }

    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<Map<String, String>> regra(RegraNegocioException ex) {
        return ResponseEntity.unprocessableEntity().body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of("erro", msg));
    }
}
