package br.com.management.api_library.exception;

public class ShelfItemNotFoundException extends RuntimeException {
    public ShelfItemNotFoundException(String message) {
        super(message);
    }
}
