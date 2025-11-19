package br.com.management.api_library.controller;

import br.com.management.api_library.dto.ShelfItemRequestByIsbnDTO;
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
@RequestMapping("/library_api/shelf")
public class ShelfController {

    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @PostMapping("/books/{bookId}")
    public ResponseEntity<ShelfItemResponseDTO> addBookToShelf(
            @PathVariable Long bookId,
            @Valid @RequestBody ShelfItemRequestDTO requestDTO,
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

    @PostMapping("/add-by-isbn")
    public ResponseEntity<ShelfItemResponseDTO> addBookToShelfByIsbn(
            @Valid @RequestBody ShelfItemRequestByIsbnDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();

        // Chama o novo serviço inteligente
        ShelfItemResponseDTO savedItem = shelfService.addBookToShelfByIsbn(username, dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/library_api/shelf/items/{id}")
                .buildAndExpand(savedItem.userBookId())
                .toUri();

        return ResponseEntity.created(location).body(savedItem);
    }

    @GetMapping("/books")
    public ResponseEntity<List<ShelfItemResponseDTO>> getMyShelf(
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        List<ShelfItemResponseDTO> shelfItems = shelfService.getUserShelf(username);
        return ResponseEntity.ok(shelfItems);
    }

    @PutMapping("/items/{userBookId}")
    public ResponseEntity<ShelfItemResponseDTO> updateBookOnShelf(
            @PathVariable Long userBookId,
            @Valid @RequestBody ShelfItemRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        ShelfItemResponseDTO updatedItem = shelfService.updateBookOnShelf(username, userBookId, requestDTO);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/items/{userBookId}")
    public ResponseEntity<Void> removeBookFromShelf(
            @PathVariable Long userBookId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        shelfService.removeBookFromShelf(username, userBookId);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}