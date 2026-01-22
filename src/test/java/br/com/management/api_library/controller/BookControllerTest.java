package br.com.management.api_library.controller;

import br.com.management.api_library.dto.BookCreateDTO;
import br.com.management.api_library.dto.BookResponseDTO;
import br.com.management.api_library.dto.GoogleBookVolumeInfo;
import br.com.management.api_library.service.BookService;
import br.com.management.api_library.service.IsbnService;
import br.com.management.api_library.service.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;
    @MockitoBean
    private IsbnService isbnService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private br.com.management.api_library.repository.UserRepository userRepository;
    @MockitoBean
    private br.com.management.api_library.repository.RoleRepository roleRepository;
    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /library_api/books - Deve criar livro (201 Created)")
    void deveCriarLivro() throws Exception {
        BookCreateDTO dto = new BookCreateDTO("Clean Code", "Uncle Bob", "Pearson", "Tech", "Desc", "EN", "978-123", 300);
        BookResponseDTO response = new BookResponseDTO(1L, "978-123", "Clean Code", "Uncle Bob", "Pearson", "Tech", "Desc", "EN", 300);

        when(bookService.createBook(any())).thenReturn(response);

        mockMvc.perform(post("/library_api/books")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /library_api/books - Deve listar paginado")
    void deveListarLivros() throws Exception {
        Page<BookResponseDTO> page = new PageImpl<>(List.of(
                new BookResponseDTO(1L, "123", "Livro 1", "Autor 1", "Ed", "G", "D", "PT", 100)
        ));
        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/library_api/books")
                        .with(user("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Livro 1"));
    }

    @Test
    @DisplayName("GET /library_api/books/search-google - Deve buscar no Google")
    void deveBuscarNoGoogle() throws Exception {
        GoogleBookVolumeInfo info = new GoogleBookVolumeInfo("Google Book", null, null, null, null, null, null, 0, null);
        Page<GoogleBookVolumeInfo> page = new PageImpl<>(List.of(info));

        when(isbnService.searchBooksByQuery(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/library_api/books/search-google")
                        .param("q", "java")
                        .with(user("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Google Book"));
    }

    @Test
    @DisplayName("GET /library_api/books/isbn/{isbn} - Deve buscar ou criar por ISBN")
    void deveBuscarPorIsbn() throws Exception {
        String isbn = "978123";
        BookResponseDTO response = new BookResponseDTO(1L, isbn, "Found Book", "Author", "Pub", "Gen", "Desc", "PT", 200);

        when(bookService.findOrCreateBookByIsbn(isbn)).thenReturn(response);

        mockMvc.perform(get("/library_api/books/isbn/" + isbn)
                        .with(user("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Found Book"));
    }
}