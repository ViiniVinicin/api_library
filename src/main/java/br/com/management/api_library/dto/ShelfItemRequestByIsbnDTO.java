package br.com.management.api_library.dto;

import br.com.management.api_library.model.ReadingStatus;
import jakarta.validation.constraints.NotBlank;

public record ShelfItemRequestByIsbnDTO(
        @NotBlank
        String isbn, // O identificador único que vem da pesquisa do Google

        ReadingStatus readingStatus,
        Double rating,
        String review,
        Integer currentPage,
        Boolean isFavorite
) {}