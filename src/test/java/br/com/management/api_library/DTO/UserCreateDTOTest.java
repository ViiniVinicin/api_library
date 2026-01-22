package br.com.management.api_library.DTO;

import br.com.management.api_library.dto.UserCreateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserCreateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        // Inicializa o validador padrão do Java (Hibernate Validator)
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve passar quando todos os dados são válidos")
    void devePassarComDadosValidos() {
        // Arrange
        UserCreateDTO dto = new UserCreateDTO(
                "vinicius",
                "senha123",
                "vinicius@email.com",
                "Vinicius Silva"
        );

        // Act
        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty(), "Não deveria haver erros de validação");
    }

    @Test
    @DisplayName("Deve falhar quando o email é inválido")
    void deveFalharEmailInvalido() {
        // Arrange - Email sem @ e sem domínio
        UserCreateDTO dto = new UserCreateDTO("user", "123456", "email-invalido", "Nome");

        // Act
        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Formato de email inválido.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Deve falhar quando a senha é muito curta")
    void deveFalharSenhaCurta() {
        // Arrange - Senha com 3 caracteres (minimo é 6)
        UserCreateDTO dto = new UserCreateDTO("user", "123", "email@teste.com", "Nome");

        // Act
        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        // Aqui verificamos se a mensagem é a esperada
        boolean temErroDeTamanho = violations.stream()
                .anyMatch(v -> v.getMessage().contains("A senha deve ter no mínimo 6 caracteres"));

        assertTrue(temErroDeTamanho);
    }

    @Test
    @DisplayName("Deve falhar quando campos obrigatórios estão vazios")
    void deveFalharCamposVazios() {
        // Arrange - Username e Senha vazios
        UserCreateDTO dto = new UserCreateDTO("", "", "email@teste.com", "Nome");

        // Act
        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        // Assert
        // Esperamos pelo menos 2 erros (username e password vazios)
        assertTrue(violations.size() >= 2);
    }
}