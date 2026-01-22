package br.com.management.api_library.controller;

import br.com.management.api_library.dto.UserCreateDTO;
import br.com.management.api_library.dto.UserResponseDTO;
import br.com.management.api_library.exception.ResourceNotFoundException;
import br.com.management.api_library.repository.RoleRepository;
import br.com.management.api_library.repository.UserRepository;
import br.com.management.api_library.service.UserService;
import br.com.management.api_library.service.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest sobe APENAS a camada web (leve e rápido)
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simula o Postman

    @Autowired
    private ObjectMapper objectMapper; // Para converter Objeto -> JSON

    @MockitoBean
    private UserService userService; // Mockamos o service (já testado)

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /users - Deve retornar 201 Created e o JSON do usuário")
    void deveCriarUsuarioComSucesso() throws Exception {
        // Arrange
        UserCreateDTO requestDto = new UserCreateDTO("viini", "senha123", "email@teste.com", "Vinicius");
        UserResponseDTO responseDto = new UserResponseDTO(1L, "viini", "email@teste.com", "Vinicius");

        when(userService.createUser(any(UserCreateDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/library_api/users")
                        .with(csrf())// O endpoint
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))) // Corpo da requisição

                .andExpect(status().isCreated()) // Espera HTTP 201
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("viini"));
    }

    @Test
    @DisplayName("GET /users/{fullName} - Deve retornar 404 quando não encontrar")
    void deveRetornar404QuandoNaoEncontrarUsuario() throws Exception {
        // Arrange
        String nome = "Inexistente";
        when(userService.getByFullName(nome)).thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        // Act & Assert
        mockMvc.perform(get("/library_api/users/search/by-fullName")
                        .param("fullName", nome) // Passa como parâmetro de busca (?fullName=Inexistente)
                        .with(user("usuarioTeste"))) // Simula usuário logado para passar pela segurança
                .andExpect(status().isNotFound());
    }
}