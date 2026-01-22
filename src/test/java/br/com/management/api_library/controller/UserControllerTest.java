package br.com.management.api_library.controller;

import br.com.management.api_library.dto.UserCreateDTO;
import br.com.management.api_library.dto.UserResponseDTO;
import br.com.management.api_library.service.UserService;
import br.com.management.api_library.service.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ActiveProfiles("test") // Garante que o initDatabase não rode
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- MOCKS DO SERVICE ---
    @MockitoBean
    private UserService userService;

    // --- MOCKS DE INFRA/SEGURANÇA (Necessários para subir o contexto) ---
    @MockitoBean private JwtService jwtService;
    @MockitoBean private br.com.management.api_library.repository.UserRepository userRepository;
    @MockitoBean private br.com.management.api_library.repository.RoleRepository roleRepository;
    @MockitoBean private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /library_api/users - Deve criar usuário (201 Created)")
    void deveCriarUsuario() throws Exception {
        UserCreateDTO request = new UserCreateDTO("viini", "senha123", "email@teste.com", "Vinicius");
        UserResponseDTO response = new UserResponseDTO(1L, "viini", "email@teste.com", "Vinicius");

        when(userService.createUser(any(UserCreateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/library_api/users")
                        .with(csrf()) // Obrigatório para POST
                        .with(user("admin").roles("ADMIN")) // Simula permissão
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("viini"));
    }

    @Test
    @DisplayName("GET /library_api/users - Deve listar todos os usuários")
    void deveListarUsuarios() throws Exception {
        UserResponseDTO userDto = new UserResponseDTO(1L, "viini", "email@teste.com", "Vinicius");
        when(userService.getAllUsers()).thenReturn(List.of(userDto));

        mockMvc.perform(get("/library_api/users")
                        .with(user("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("viini"));
    }

    @Test
    @DisplayName("GET /library_api/users/search/by-fullName - Deve buscar por nome completo")
    void deveBuscarPorNomeCompleto() throws Exception {
        String nome = "Vinicius";
        UserResponseDTO response = new UserResponseDTO(1L, "viini", "email@teste.com", "Vinicius");

        when(userService.getByFullName(nome)).thenReturn(response);

        mockMvc.perform(get("/library_api/users/search/by-fullName")
                        .param("fullName", nome) // Query Param
                        .with(user("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Vinicius"));
    }

    @Test
    @DisplayName("PUT /library_api/users/{id} - Deve atualizar usuário")
    void deveAtualizarUsuario() throws Exception {
        Long id = 1L;
        UserCreateDTO updateRequest = new UserCreateDTO("viini_up", "senhaNova", "email@up.com", "Vinicius Up");
        UserResponseDTO response = new UserResponseDTO(id, "viini_up", "email@up.com", "Vinicius Up");

        when(userService.updateUser(eq(id), any(UserCreateDTO.class))).thenReturn(response);

        mockMvc.perform(put("/library_api/users/" + id)
                        .with(csrf()) // Obrigatório para PUT
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("viini_up"));
    }

    @Test
    @DisplayName("DELETE /library_api/users/{id} - Deve deletar usuário (204 No Content)")
    void deveDeletarUsuario() throws Exception {
        Long id = 1L;
        doNothing().when(userService).deleteUser(id);

        mockMvc.perform(delete("/library_api/users/" + id)
                        .with(csrf()) // Obrigatório para DELETE
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent()); // Espera 204
    }
}