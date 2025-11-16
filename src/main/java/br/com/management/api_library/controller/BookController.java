package br.com.management.api_library.controller;

import br.com.management.api_library.dto.BookCreateDTO;
import br.com.management.api_library.dto.BookResponseDTO;
import br.com.management.api_library.dto.BookUpdateDTO;
import br.com.management.api_library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/library_api/books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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
    public ResponseEntity<List<BookResponseDTO>> findAll() {
        List<BookResponseDTO> booksDTO = bookService.getAllBooks();
        return ResponseEntity.ok(booksDTO);
    }

    @GetMapping("/by-title")
    public ResponseEntity<BookResponseDTO> findByTitle(@RequestParam("title") String title) {
        BookResponseDTO bookDTO = bookService.getBookByTitle(title);
        return ResponseEntity.ok(bookDTO);
    }

    @GetMapping("/by-genre")
    public ResponseEntity<List<BookResponseDTO>> findByGenre(@RequestParam("genre") String genre) {
        List<BookResponseDTO> booksDTO = bookService.getBooksByGenre(genre);
        return ResponseEntity.ok(booksDTO);
    }

    @GetMapping("/by-author")
    public ResponseEntity<List<BookResponseDTO>> findByAuthor(@RequestParam("author") String author) {
        List<BookResponseDTO> bookDTO = bookService.getByAuthor(author);
        return ResponseEntity.ok(bookDTO);
    }

    @GetMapping("/by-publisher")
    public ResponseEntity<List<BookResponseDTO>> findByPublisher(@RequestParam("publisher") String publisher) {
        List<BookResponseDTO> bookDTO = bookService.getByPublisher(publisher);
        return ResponseEntity.ok(bookDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> update(@PathVariable Long id, @Valid @RequestBody BookUpdateDTO bookUpdateDTO) {
        BookResponseDTO updatedBook = bookService.updateBook(id, bookUpdateDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // ISBN

    @GetMapping("/isbn/{isbn}") // URL será /library_api/books/isbn/9788576082675
    public ResponseEntity<BookResponseDTO> findBookByIsbn(@PathVariable String isbn) {
        BookResponseDTO bookDTO = bookService.findOrCreateBookByIsbn(isbn);
        return ResponseEntity.ok(bookDTO);
    }
}
