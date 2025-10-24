package br.com.management.api_library.exception;

public class BookAlreadyExistsOnShelfException extends RuntimeException {
    public BookAlreadyExistsOnShelfException(String message) {
        super(message);
    }
}
