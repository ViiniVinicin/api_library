package br.com.management.api_library.controller;

import br.com.management.api_library.dto.ShelfItemRequestDTO;
import br.com.management.api_library.dto.ShelfItemResponseDTO;
import br.com.management.api_library.service.ShelfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/library_api/shelf") // Ajustei a URL base para /shelf
public class ShelfController {

    private final ShelfService shelfService;

    // @Autowired é opcional
    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    /**
     * Adiciona um livro (pelo ID do livro) à estante do usuário autenticado.
     */
    @PostMapping("/books/{bookId}")
    public ResponseEntity<ShelfItemResponseDTO> addBookToShelf(
            @PathVariable Long bookId,
            @Valid @RequestBody ShelfItemRequestDTO requestDTO, // DTO com status inicial, etc.
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        ShelfItemResponseDTO savedItem = shelfService.addBookToShelf(username, bookId, requestDTO);

        // Cria a URI para o item específico da estante que foi criado
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/library_api/shelf/items/{id}") // Novo path para o item específico
                .buildAndExpand(savedItem.userBookId())
                .toUri();

        return ResponseEntity.created(location).body(savedItem);
    }

    /**
     * Lista todos os itens da estante do usuário autenticado.
     */
    @GetMapping("/books") // Mudança: Endpoint para listar todos os livros na estante
    public ResponseEntity<List<ShelfItemResponseDTO>> getMyShelf(
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        List<ShelfItemResponseDTO> shelfItems = shelfService.getUserShelf(username);
        return ResponseEntity.ok(shelfItems);
    }

    /**
     * Atualiza um item específico na estante (pelo ID da relação UserBook).
     */
    @PutMapping("/items/{userBookId}") // Mudança: Path e ID para o item da estante
    public ResponseEntity<ShelfItemResponseDTO> updateBookOnShelf(
            @PathVariable Long userBookId,
            @Valid @RequestBody ShelfItemRequestDTO requestDTO, // DTO com os novos dados
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        ShelfItemResponseDTO updatedItem = shelfService.updateBookOnShelf(username, userBookId, requestDTO);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * Remove um item específico da estante (pelo ID da relação UserBook).
     */
    @DeleteMapping("/items/{userBookId}") // Mudança: Path e ID para o item da estante
    public ResponseEntity<Void> removeBookFromShelf(
            @PathVariable Long userBookId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        shelfService.removeBookFromShelf(username, userBookId);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}