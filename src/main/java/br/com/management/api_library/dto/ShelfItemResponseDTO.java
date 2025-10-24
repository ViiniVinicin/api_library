package br.com.management.api_library.dto;
import br.com.management.api_library.model.ReadingStatus;
public record ShelfItemResponseDTO(
        Long userBookId,
        Long bookId,
        String title,
        String author,
        ReadingStatus readingStatus,
        Double rating,
        String review,
        Boolean isFavorite,
        Integer currentPage
) {}