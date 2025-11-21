package br.com.management.api_library.exception;

public class JwtTokenGenerateErrorException extends RuntimeException {
    public JwtTokenGenerateErrorException(String message) {
        super(message);
    }
}
