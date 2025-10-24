package br.com.management.api_library.exception;

public class UnauthorizedShelfAccessException extends RuntimeException {
    public UnauthorizedShelfAccessException(String message) {
        super(message);
    }
}
