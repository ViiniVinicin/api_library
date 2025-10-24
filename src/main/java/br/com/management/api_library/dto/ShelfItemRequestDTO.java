package br.com.management.api_library.dto;
import br.com.management.api_library.model.ReadingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ShelfItemRequestDTO(

        @NotNull(message = "O status de leitura é obrigatório.")
        ReadingStatus readingStatus,
        @Min(value = 1, message = "A nota deve ser no mínimo 1.")
        @Max(value = 5, message = "A nota deve ser no máximo 5.")
        Double rating,
        String review,
        @Min(value = 0, message = "A página atual não pode ser negativa.")
        Integer currentPage,
        Boolean isFavorite
) {}