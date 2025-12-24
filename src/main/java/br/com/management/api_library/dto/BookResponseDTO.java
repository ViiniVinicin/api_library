package br.com.management.api_library.dto;

public record BookResponseDTO(
        Long id,
        String isbn,
        String title,
        String author,
        String publisher,
        String genre,
        String description,
        String language,
        int pages
) {
}
