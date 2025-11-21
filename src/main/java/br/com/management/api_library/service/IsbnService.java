package br.com.management.api_library.service;

import br.com.management.api_library.dto.GoogleBookApiResponse;
import br.com.management.api_library.dto.GoogleBookVolumeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    public Page<GoogleBookVolumeInfo> searchBooksByQuery(String query, Pageable pageable) {
        String cleanQuery = query.trim();
        if (cleanQuery.matches("^\\d{10}|\\d{13}$")) {
            cleanQuery = "isbn:" + cleanQuery;
        }

        // --- CÁLCULO DA PAGINAÇÃO ---
        // O Spring usa páginas baseadas em 0 (0, 1, 2...)
        // O Google usa um índice baseado em 0 para o primeiro item.
        // startIndex = número da página * tamanho da página
        int startIndex = pageable.getPageNumber() * pageable.getPageSize();
        int maxResults = pageable.getPageSize();

        log.info("Buscando no Google: query='{}', startIndex={}, maxResults={}", cleanQuery, startIndex, maxResults);

        try {
            String finalQuery = cleanQuery;
            GoogleBookApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes")
                            .queryParam("q", finalQuery)
                            .queryParam("startIndex", startIndex) // Adiciona o início
                            .queryParam("maxResults", maxResults) // Adiciona o tamanho
                            .queryParam("key", this.apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(GoogleBookApiResponse.class)
                    .block();

            List<GoogleBookVolumeInfo> results = List.of();
            long totalItems = (pageable.getPageNumber() + 2) * pageable.getPageSize(); // O Google retorna o total aproximado de resultados

            if (response != null) {
                // 1. Capturamos o total de itens que o Google diz ter
                log.info("Total de itens retornado pelo Google: {}", response.totalItems());
                totalItems = response.totalItems();

                if (response.items() != null) {
                    results = response.items().stream()
                            .map(item -> item.volumeInfo())
                            .filter(info -> info.imageLinks() != null)
                            .collect(Collectors.toList());
                }
            }

            // Retorna um objeto Page do Spring, que contém a lista e os metadados
            // Nota: Sem o totalItems real, a paginação no front pode ficar imprecisa (sem saber quantas páginas existem).
            // Para simplificar agora, podemos retornar uma PageImpl com o tamanho da lista atual.
            return new PageImpl<>(results, pageable, totalItems );

        } catch (Exception e) {
            log.error("Erro na busca: {}", e.getMessage());
            return Page.empty();
        }
    }
}