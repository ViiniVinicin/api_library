package br.com.management.api_library.exception;

public class BookAlreadyOnShelfException extends RuntimeException {
    public BookAlreadyOnShelfException(String message) {
        super(message);
    }
}
