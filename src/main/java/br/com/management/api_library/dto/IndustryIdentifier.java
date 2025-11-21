package br.com.management.api_library.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IndustryIdentifier(
        String type,
        String identifier
) {}