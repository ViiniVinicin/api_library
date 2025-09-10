package br.com.management.api_library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Usaremos apenas Getters, pois os dados serão imutáveis após a criação.
import lombok.Getter;

@Getter
public class UserCreateDTO {

    @NotBlank(message = "O nome de usuário não pode ser vazio.")
    private String username;

    @NotBlank(message = "A senha não pode ser vazia.")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    private String password;

    @NotBlank(message = "O email não pode ser vazio.")
    @Email(message = "Formato de email inválido.")
    private String email;

    private String fullName;
}