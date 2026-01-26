package br.com.management.api_library.service;

import br.com.management.api_library.dto.GoogleBookVolumeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class IsbnServiceTest {

    private MockWebServer mockWebServer;
    private IsbnService isbnService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        // 1. Sobe o servidor fake na porta livre
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // 2. Pega a URL do servidor fake (ex: http://localhost:54321)
        String baseUrl = mockWebServer.url("/").toString();

        // 3. Cria o WebClient apontando para o servidor fake em vez do Google
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        isbnService = new IsbnService(webClient, "CHAVE_FALSA_PARA_TESTE");

        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Desliga o servidor no final do teste
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Deve retornar os dados do livro quando a API do Google responder com sucesso")
    void deveRetornarLivroComSucesso() throws Exception {
        // ARRANGE: Criamos o JSON fake que o "Google" vai responder
        String jsonFakeResponse = """
            {
              "items": [
                {
                  "volumeInfo": {
                    "title": "Java Spring Boot",
                    "authors": ["Vinicius"],
                    "publisher": "Tech Books",
                    "description": "Livro de Spring",
                    "language": "pt",
                    "pageCount": 250
                  }
                }
              ]
            }
            """;

        // Enfileira a resposta fake no MockWebServer com Status 200 OK
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonFakeResponse)
                .addHeader("Content-Type", "application/json"));

        // ACT: Chama o serviço (que vai bater no nosso servidor fake)
        String isbn = "9781234567890";
        Optional<GoogleBookVolumeInfo> result = isbnService.findBookInfoByIsbn(isbn);

        // ASSERT: Verifica se o JSON foi convertido perfeitamente para o seu DTO
        assertTrue(result.isPresent());
        assertEquals("Java Spring Boot", result.get().title());
        assertEquals(List.of("Vinicius"), result.get().authors());
        assertEquals(250, result.get().pageCount());
    }

    @Test
    @DisplayName("Deve retornar Vazio (Optional.empty) quando o livro não existir (items: null)")
    void deveRetornarVazioQuandoLivroNaoExistir() {
        // ARRANGE: JSON que o Google retorna quando não acha o livro (sem a chave "items")
        String jsonFakeEmptyResponse = """
            {
              "totalItems": 0
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonFakeEmptyResponse)
                .addHeader("Content-Type", "application/json"));

        // ACT
        Optional<GoogleBookVolumeInfo> result = isbnService.findBookInfoByIsbn("0000000000000");

        // ASSERT
        assertTrue(result.isEmpty());
    }
}