package br.com.management.api_library.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandlerException extends RuntimeException {

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value()); // 404
        body.put("error", "Recurso não Encontrado");
        body.put("message", ex.getMessage());
        body.put("path", "/library_api/books/" + ex.getMessage().replaceAll("[^0-9]", "")); // Bônus: extraindo o ID da msg

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Map<String, Object>> handleBookAlreadyExistsException(BookAlreadyExistsException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Recurso já cadastrado");
        body.put("message", ex.getMessage());
        body.put("path", "/library_api/books/" + ex.getMessage().replaceAll("[^0-9]", ""));

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }
}
