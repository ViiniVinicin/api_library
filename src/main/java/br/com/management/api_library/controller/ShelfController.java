package br.com.management.api_library.controller;

import br.com.management.api_library.dto.ShelfItemRequestByIsbnDTO;
import br.com.management.api_library.dto.ShelfItemRequestDTO;
import br.com.management.api_library.dto.ShelfItemResponseDTO;
import br.com.management.api_library.model.User;
import br.com.management.api_library.service.ShelfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/library_api/shelf")
@Tag(name = "Minha Estante", description = "Gerenciamento da leitura pessoal do usuário logado (adicionar, avaliar, status)")
public class ShelfController {

    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @PostMapping("/books/{bookId}")
    @Operation(summary = "Adicionar Livro (pelo ID)", description = "Adiciona um livro que JÁ existe no catálogo à estante do usuário.")
    public ResponseEntity<ShelfItemResponseDTO> addBookToShelf(
            @PathVariable Long bookId,
            @Valid @RequestBody ShelfItemRequestDTO requestDTO,
            @AuthenticationPrincipal User user
    ) {

        String username = user.getUsername();
        ShelfItemResponseDTO savedItem = shelfService.addBookToShelf(username, bookId, requestDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/library_api/shelf/items/{id}")
                .buildAndExpand(savedItem.userBookId())
                .toUri();

        return ResponseEntity.created(location).body(savedItem);
    }

    @PostMapping("/add-by-isbn")
    @Operation(summary = "Adicionar Livro (pelo ISBN)", description = "Busca Inteligente: Se o livro não existir, baixa do Google Books e adiciona à estante automaticamente.")
    public ResponseEntity<ShelfItemResponseDTO> addBookToShelfByIsbn(
            @Valid @RequestBody ShelfItemRequestByIsbnDTO dto,
            @AuthenticationPrincipal User user) {

        String username = user.getUsername();
        ShelfItemResponseDTO savedItem = shelfService.addBookToShelfByIsbn(username, dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/library_api/shelf/items/{id}")
                .buildAndExpand(savedItem.userBookId())
                .toUri();

        return ResponseEntity.created(location).body(savedItem);
    }

    @GetMapping("/books")
    @Operation(summary = "Ver minha estante", description = "Retorna a lista paginada de livros que o usuário está lendo, já leu ou quer ler.")
    public ResponseEntity<Page<ShelfItemResponseDTO>> getMyShelf(
            @AuthenticationPrincipal User user,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String username = user.getUsername();
        Page<ShelfItemResponseDTO> shelfItems = shelfService.getUserShelf(username, pageable);
        return ResponseEntity.ok(shelfItems);
    }

    @PutMapping("/items/{userBookId}")
    @Operation(summary = "Atualizar leitura", description = "Atualiza o status de leitura (Lendo, Lido), nota e review de um item da estante.")
    public ResponseEntity<ShelfItemResponseDTO> updateBookOnShelf(
            @PathVariable Long userBookId,
            @Valid @RequestBody ShelfItemRequestDTO requestDTO,
            @AuthenticationPrincipal User user) {

        String username = user.getUsername();
        ShelfItemResponseDTO updatedItem = shelfService.updateBookOnShelf(username, userBookId, requestDTO);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/items/{userBookId}")
    @Operation(summary = "Remover da estante", description = "Remove um livro da estante do usuário (não apaga o livro do catálogo global).")
    public ResponseEntity<Void> removeBookFromShelf(
            @PathVariable Long userBookId,
            @AuthenticationPrincipal User user) {

        String username = user.getUsername();
        shelfService.removeBookFromShelf(username, userBookId);
        return ResponseEntity.noContent().build();
    }
}