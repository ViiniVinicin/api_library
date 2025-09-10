package br.com.management.api_library.dto;

public record UserResponseDTO (
        Long id,
        String username,
        String email,
        String fullName
){}