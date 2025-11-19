package br.com.management.api_library.service;

import br.com.management.api_library.dto.GoogleBookApiResponse;
import br.com.management.api_library.dto.GoogleBookVolumeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IsbnService {

    private static final Logger log = LoggerFactory.getLogger(IsbnService.class);
    private final WebClient webClient;
    private final String apiKey;

    public IsbnService(WebClient webClient, @Value("${google.books.api.key}") String apiKey) {
        this.webClient = webClient;
        this.apiKey = apiKey;
    }

    /**
     * Busca informações de um livro na API do Google Books pelo ISBN.
     * Retorna um Optional contendo as informações do volume, ou vazio se não encontrar.
     */
    public Optional<GoogleBookVolumeInfo> findBookInfoByIsbn(String isbn) {
        log.info("Buscando informações para o ISBN: {}", isbn);
        String queryParam = "isbn:" + isbn;
        String finalUri = "/volumes?q=" + queryParam + "&key=" + this.apiKey;
        log.debug("Chamando Google Books API com URI: {}", finalUri);

        try {

            // Faz a chamada GET para a API
            GoogleBookApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes") // Já temos a baseUrl configurada
                            .queryParam("q", queryParam)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve() // Executa a requisição
                    .onStatus(status -> status.isError(), clientResponse -> {
                        log.error("Erro recebido da API do Google Books: Status {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Corpo do erro da API do Google: {}", errorBody);
                                    return Mono.error(new RuntimeException("Erro da API externa: " + clientResponse.statusCode()));
                                });
                    })
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

    public List<GoogleBookVolumeInfo> searchBooksByQuery(String query) {
        log.info("Buscando livros no Google Books com a query: {}", query);

        try {
            GoogleBookApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes")
                            .queryParam("q", query) // O Google é inteligente, 'q' busca em tudo
                            .queryParam("maxResults", 10) // Limita a 10 sugestões para o autocomplete
                            .queryParam("key", this.apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(GoogleBookApiResponse.class)
                    .block();

            if (response != null && response.items() != null) {
                // Mapeia a lista de itens para retornar apenas os dados do volume
                return response.items().stream()
                        .map(item -> item.volumeInfo())
                        // Filtra resultados que não tenham ISBN, pois precisamos dele depois
                        .filter(info -> info.imageLinks() != null) // Opcional: filtrar sem capa
                        .collect(Collectors.toList());
            }
            return List.of(); // Retorna lista vazia se nada for encontrado
        } catch (Exception e) {
            log.error("Erro ao buscar livros por query: {}", e.getMessage());
            return List.of();
        }
    }
}