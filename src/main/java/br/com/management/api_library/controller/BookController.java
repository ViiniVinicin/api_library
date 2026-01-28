package br.com.management.api_library.controller;

import br.com.management.api_library.dto.BookCreateDTO;
import br.com.management.api_library.dto.BookResponseDTO;
import br.com.management.api_library.dto.BookUpdateDTO;
import br.com.management.api_library.dto.GoogleBookVolumeInfo;
import br.com.management.api_library.service.BookService;
import br.com.management.api_library.service.IsbnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/library_api/books")
@Tag(name = "Catálogo de Livros", description = "Gestão do acervo global da biblioteca (CRUD de livros)")
public class BookController {

    private final BookService bookService;
    private final IsbnService isbnService;

    @Autowired
    public BookController(BookService bookService,  IsbnService isbnService) {
        this.bookService = bookService;
        this.isbnService = isbnService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar Livro Manualmente", description = "Cria um novo livro no sistema passando todos os dados manualmente.")
    public ResponseEntity<BookResponseDTO> create(@Valid @RequestBody BookCreateDTO bookCreateDTO) {
        BookResponseDTO createBook = bookService.createBook(bookCreateDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createBook.id())
                .toUri();
        return ResponseEntity.created(location).body(createBook);
    }

    @GetMapping
    @Operation(summary = "Listar Catálogo", description = "Retorna todos os livros do sistema de forma paginada.")
    public ResponseEntity<Page<BookResponseDTO>> findAll(
            @ParameterObject
            @PageableDefault(page = 0, size = 20, sort = "title") Pageable pageable
    ) {
        Page<BookResponseDTO> booksDTO = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(booksDTO);
    }

    @GetMapping("/search-google")
    @Operation(summary = "Buscar no Google Books API", description = "Faz uma pesquisa externa no Google Books (não salva no banco, apenas consulta).")
    public ResponseEntity<Page<GoogleBookVolumeInfo>> searchGoogleBooks(
            @RequestParam("Nome do Livro") String query,
            @ParameterObject
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ){
        Page<GoogleBookVolumeInfo> results = isbnService.searchBooksByQuery(query, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/by-title")
    @Operation(summary = "Buscar por Título", description = "Procura um livro específico pelo título exato ou parcial.")
    public ResponseEntity<BookResponseDTO> findByTitle(@RequestParam("title") String title) {
        BookResponseDTO bookDTO = bookService.getBookByTitle(title);
        return ResponseEntity.ok(bookDTO);
    }

    @GetMapping("/by-genre")
    @Operation(summary = "Filtrar por Gênero", description = "Retorna livros de um determinado gênero.")
    public ResponseEntity<Page<BookResponseDTO>> findByGenre(
            @RequestParam("genre") String genre,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<BookResponseDTO> booksDTO = bookService.getBooksByGenre(genre, pageable);
        return ResponseEntity.ok(booksDTO);
    }

    @GetMapping("/by-author")
    @Operation(summary = "Filtrar por Autor", description = "Retorna lista de livros de um autor específico.")
    public ResponseEntity<List<BookResponseDTO>> findByAuthor(@RequestParam("author") String author) {
        List<BookResponseDTO> bookDTO = bookService.getByAuthor(author);
        return ResponseEntity.ok(bookDTO);
    }

    @GetMapping("/by-publisher")
    @Operation(summary = "Filtrar por Editora", description = "Retorna lista de livros publicados por uma editora.")
    public ResponseEntity<List<BookResponseDTO>> findByPublisher(@RequestParam("publisher") String publisher) {
        List<BookResponseDTO> bookDTO = bookService.getByPublisher(publisher);
        return ResponseEntity.ok(bookDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Livro", description = "Atualiza os dados cadastrais de um livro.")
    public ResponseEntity<BookResponseDTO> update(@PathVariable Long id, @Valid @RequestBody BookUpdateDTO bookUpdateDTO) {
        BookResponseDTO updatedBook = bookService.updateBook(id, bookUpdateDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Livro", description = "Remove um livro do catálogo (Cuidado: pode afetar estantes de usuários).")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Buscar ou Importar por ISBN", description = "Busca no banco local. Se não achar, busca no Google, salva no banco e retorna.")
    public ResponseEntity<BookResponseDTO> findBookByIsbn(@PathVariable String isbn) {
        BookResponseDTO bookDTO = bookService.findOrCreateBookByIsbn(isbn);
        return ResponseEntity.ok(bookDTO);
    }
}