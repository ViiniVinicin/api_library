package br.com.management.api_library.controller;

import br.com.management.api_library.dto.ShelfItemRequestDTO;
import br.com.management.api_library.dto.ShelfItemResponseDTO;
import br.com.management.api_library.model.ReadingStatus;
import br.com.management.api_library.service.ShelfService;
import br.com.management.api_library.service.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShelfController.class)
@ActiveProfiles("test")
class ShelfControllerTest {

    // Configuração manual do MockMvc para contornar o problema do @AuthenticationPrincipal com entidade customizada
    private MockMvc mockMvc;

    @Autowired
    private ShelfController shelfController;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShelfService shelfService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private br.com.management.api_library.repository.UserRepository userRepository;
    @MockitoBean private br.com.management.api_library.repository.RoleRepository roleRepository;
    @MockitoBean private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Criamos um resolvedor de argumentos customizado apenas para o teste
        // Ele vai injetar a NOSSA entidade User sempre que ver @AuthenticationPrincipal
        HandlerMethodArgumentResolver userArgumentResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(br.com.management.api_library.model.User.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                br.com.management.api_library.model.User user = new br.com.management.api_library.model.User();
                user.setUsername("vinicius");
                return user;
            }
        };

        // Construímos o MockMvc manualmente com esse resolvedor
        this.mockMvc = MockMvcBuilders.standaloneSetup(shelfController)
                .setCustomArgumentResolvers(userArgumentResolver)
                .build();
    }

    @Test
    @DisplayName("POST /library_api/shelf/books/{id} - Deve adicionar livro à estante")
    void deveAdicionarLivroEstante() throws Exception {
        Long bookId = 1L;
        ShelfItemRequestDTO request = new ShelfItemRequestDTO(ReadingStatus.READING, 5.0, "Bom", 10, true);
        ShelfItemResponseDTO response = new ShelfItemResponseDTO(10L, bookId, "Title", "Auth", ReadingStatus.READING, 5.0, "Bom", true, 10);

        when(shelfService.addBookToShelf(eq("vinicius"), eq(bookId), any())).thenReturn(response);

        mockMvc.perform(post("/library_api/shelf/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userBookId").value(10L));
    }
}