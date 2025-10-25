package br.com.management.api_library.service;

import br.com.management.api_library.dto.GoogleBookApiResponse;
import br.com.management.api_library.dto.GoogleBookVolumeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Optional;

@Service
public class IsbnService {

    private static final Logger log = LoggerFactory.getLogger(IsbnService.class);
    private final WebClient webClient;

    public IsbnService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Busca informações de um livro na API do Google Books pelo ISBN.
     * Retorna um Optional contendo as informações do volume, ou vazio se não encontrar.
     */
    public Optional<GoogleBookVolumeInfo> findBookInfoByIsbn(String isbn) {
        log.info("Buscando informações para o ISBN: {}", isbn);
        String queryParam = "isbn:" + isbn;

        try {
            // Faz a chamada GET para a API
            GoogleBookApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes") // Já temos a baseUrl configurada
                            .queryParam("book", queryParam)
                            .build())
                    .retrieve() // Executa a requisição
                    .bodyToMono(GoogleBookApiResponse.class) // Mapeia o corpo para nosso DTO
                    .block(); // Espera a resposta (forma síncrona simples para começar)

            // Processa a resposta
            if (response != null && response.items() != null && !response.items().isEmpty()) {
                log.info("Informações encontradas para o ISBN: {}", isbn);
                // Retorna o volumeInfo do primeiro item encontrado
                return Optional.ofNullable(response.items().get(0).volumeInfo());
            } else {
                log.warn("Nenhuma informação encontrada para o ISBN: {}", isbn);
                return Optional.empty();
            }
        } catch (Exception e) {
            // Loga o erro se a chamada à API falhar
            log.error("Erro ao chamar a API do Google Books para o ISBN {}: {}", isbn, e.getMessage());
            return Optional.empty(); // Retorna vazio em caso de erro
        }
    }
}