package br.com.management.api_library.dto; // Verifique se o seu pacote é este

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // Anotação MÁGICA!
public class ErrorResponseDTO {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private Map<String, String> fieldErrors; // Campo opcional

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this(timestamp, status, error, message, path);
        this.fieldErrors = fieldErrors;
    }
}