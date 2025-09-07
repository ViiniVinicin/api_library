package br.com.management.api_library.dto;

public record BookDTO(
        Long id,
        String title,
        String author,
        String publisher,
        String genre,
        String description,
        String language,
        String isbn,
        int pages
) {}
