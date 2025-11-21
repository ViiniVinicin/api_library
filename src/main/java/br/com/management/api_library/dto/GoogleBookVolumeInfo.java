package br.com.management.api_library.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map; // Para imageLinks

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleBookVolumeInfo(
        String title,
        List<String> authors,
        String publisher,
        String description,
        Map<String, String> imageLinks, // imageLinks é um objeto JSON
        List<String> categories,
        String language,
        int pageCount,
        List<IndustryIdentifier> industryIdentifiers
) {}