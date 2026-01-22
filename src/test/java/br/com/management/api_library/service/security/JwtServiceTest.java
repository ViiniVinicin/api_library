package br.com.management.api_library.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Como não subimos o Spring, o @Value é null. Injetamos manualmente via Reflection.
        // O segredo precisa ser forte para o algoritmo HMAC256
        ReflectionTestUtils.setField(jwtService, "secret", "minhaChaveSecretaSuperSegura123456789");
    }

    @Test
    @DisplayName("Deve gerar um token válido para um usuário")
    void deveGerarTokenComSucesso() {
        // Arrange
        // 1. Criamos um Mock (um dublê) da interface UserDetails
        UserDetails userMock = mock(UserDetails.class);

        // 2. Ensinamos ao mock: "Quando perguntarem seu nome, diga 'vinicius'"
        when(userMock.getUsername()).thenReturn("vinicius");

        // Act
        // Passamos o mock, que agora é do tipo correto (UserDetails)
        String token = jwtService.generateToken(userMock);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Deve validar token e retornar o username correto")
    void deveValidarTokenComSucesso() {
        // Arrange
        UserDetails userMock = mock(UserDetails.class);
        when(userMock.getUsername()).thenReturn("vinicius");

        // Gera o token usando o mock validado
        String token = jwtService.generateToken(userMock);

        // Act
        String usernameRecuperado = jwtService.validateToken(token);

        // Assert
        assertEquals("vinicius", usernameRecuperado);
    }

    @Test
    @DisplayName("Deve retornar vazio ao validar token inválido")
    void deveRetornarVazioSeTokenInvalido() {
        // Act
        String resultado = jwtService.validateToken("token.invalido.123");

        // Assert
        assertEquals("", resultado);
    }
}