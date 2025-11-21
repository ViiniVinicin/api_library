package br.com.management.api_library.dto;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleBookApiResponse(
        List<GoogleBookItem> items,
        int totalItems
) {}