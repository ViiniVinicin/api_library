package br.com.management.api_library.dto;

public record BookUpdateDTO (
        String bookTitle,
        String author,
        String publisher,
        String genre,
        String description,
        String language,
        String isbn,
        int pages,
        int publicationYear,
        int availableCopies
){
}