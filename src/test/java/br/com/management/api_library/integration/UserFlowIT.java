package br.com.management.api_library.integration;

import br.com.management.api_library.dto.LoginRequestDTO;
import br.com.management.api_library.dto.UserCreateDTO;
import br.com.management.api_library.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest sobe o contexto completo da aplicação (Service, Repository, Security, tudo)
@SpringBootTest
@AutoConfigureMockMvc // Configura o MockMvc para bater nos Controllers reais
@ActiveProfiles("test") // Usa o application-test.properties (H2) e desativa o initDatabase
class UserFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository; // Injetamos o REPOSITÓRIO REAL para conferir o banco

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve completar o ciclo: Registrar Usuário -> Salvar no Banco -> Autenticar")
    @Sql(scripts = "/import-roles.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deveRegistrarELogarUsuarioComSucesso() throws Exception {

        // --- CENÁRIO 1: REGISTRO ---
        UserCreateDTO novoUsuario = new UserCreateDTO(
                "usuario_integracao",
                "123456",
                "integracao@teste.com",
                "Teste Integração"
        );

        // 1. Faz o POST para criar (Simulando o Frontend)
        mockMvc.perform(post("/library_api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoUsuario)))
                .andExpect(status().isCreated()) // Espera 201
                .andExpect(jsonPath("$.id").exists()); // Espera que retorne um ID

        // 2. VERIFICAÇÃO NO BANCO DE DADOS (Prova Real)
        // Aqui não usamos Mockito.when(). Aqui vamos no banco ver se salvou mesmo!
        assertTrue(userRepository.findByUsername("usuario_integracao").isPresent(),
                "O usuário deveria ter sido salvo no banco H2");


        // --- CENÁRIO 2: LOGIN ---
        LoginRequestDTO loginData = new LoginRequestDTO("usuario_integracao", "123456");

        // 3. Tenta fazer login com o usuário que acabamos de criar
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.token").exists()) // Espera receber um JWT
                .andExpect(jsonPath("$.token").isString());
    }
}