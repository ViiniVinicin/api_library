package br.com.management.api_library.controller;

import br.com.management.api_library.dto.LoginRequestDTO;
import br.com.management.api_library.service.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // <--- Importe
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
// Não precisamos mais do csrf() pois desligaremos os filtros
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // <--- O PULO DO GATO: Desliga a barreira de segurança
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    // Ainda precisamos mockar os componentes que o contexto pede para subir,
    // mesmo com os filtros desligados, para evitar erros de injeção.
    @MockitoBean private br.com.management.api_library.repository.UserRepository userRepository;
    @MockitoBean private br.com.management.api_library.repository.RoleRepository roleRepository;
    @MockitoBean private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /auth/login - Deve retornar Token quando credenciais válidas")
    void deveLogarComSucesso() throws Exception {
        // Arrange
        LoginRequestDTO loginDto = new LoginRequestDTO("user", "senha123");
        String tokenEsperado = "token.jwt.valido";

        // Mock do AuthenticationManager
        Authentication authMock = mock(Authentication.class);
        UserDetails userDetails = new User("user", "senha123", Collections.emptyList());

        when(authMock.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);

        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(tokenEsperado);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        //.with(csrf()) <--- REMOVIDO: Com filtros desligados, não precisa de CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk()) // Agora esperamos 200 OK de verdade
                .andExpect(jsonPath("$.token").value(tokenEsperado));
    }
}