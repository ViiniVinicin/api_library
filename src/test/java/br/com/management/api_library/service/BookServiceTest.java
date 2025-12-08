package br.com.management.api_library.service;

import br.com.management.api_library.dto.BookCreateDTO;
import br.com.management.api_library.dto.BookResponseDTO;
import br.com.management.api_library.exception.BookAlreadyExistsException;
import br.com.management.api_library.exception.ResourceNotFoundException;
import br.com.management.api_library.model.Book;
import br.com.management.api_library.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    // --- TESTE 1: REGRA DE NEGÓCIO (DUPLICIDADE) ---
    @Test
    @DisplayName("Deve lançar erro quando tentar criar livro com ISBN já existente")
    void shouldThrowExceptionWhenIsbnExists() {
        // ARRANGE
        String isbn = "978-123";
        BookCreateDTO dto = new BookCreateDTO(
                "Title",
                "Auth",
                "Pub",
                "Gen",
                "Desc",
                "PT",
                isbn,
                100);

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(new Book()));

        // ACT & ASSERT
        assertThrows(BookAlreadyExistsException.class, () -> bookService.createBook(dto));
        verify(bookRepository, never()).save(any());
    }

    // --- TESTE 2: SALVAR COM SUCESSO (CREATE) ---
    @Test
    @DisplayName("Deve salvar um livro com sucesso quando ISBN for único")
    void shouldCreateBookSuccess() {
        // ARRANGE
        BookCreateDTO dto = new BookCreateDTO("Clean Code", "Uncle Bob", "Pearson", "Tech", "Coding", "EN", "978-NEW", 300);

        // Simulamos o livro que o banco "devolveria" após salvar (com ID)
        Book bookSalvo = new Book();
        bookSalvo.setId(1L);
        bookSalvo.setTitle("Clean Code");
        bookSalvo.setIsbn("978-NEW");

        // 1. Não acha ISBN duplicado
        when(bookRepository.findByIsbn(dto.isbn())).thenReturn(Optional.empty());
        // 2. Salva e retorna o objeto com ID
        when(bookRepository.save(any(Book.class))).thenReturn(bookSalvo);

        // ACT
        BookResponseDTO response = bookService.createBook(dto);

        // ASSERT
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Clean Code", response.title());
        verify(bookRepository, times(1)).save(any());
    }

    // --- TESTE 3: BUSCAR POR ID COM SUCESSO (READ) ---
    @Test
    @DisplayName("Deve retornar um livro quando o ID existir")
    void shouldReturnBookWhenIdExists() {
        // ARRANGE
        Long id = 1L;
        Book book = new Book();
        book.setId(id);
        book.setTitle("Harry Potter");

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        // ACT
        BookResponseDTO response = bookService.getById(id);

        // ASSERT
        assertNotNull(response);
        assertEquals("Harry Potter", response.title());
    }

    // --- TESTE 4: BUSCAR POR ID INEXISTENTE (READ - ERROR) ---
    @Test
    @DisplayName("Deve lançar erro quando buscar ID inexistente")
    void shouldThrowExceptionWhenIdNotFound() {
        // ARRANGE
        Long id = 99L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> bookService.getById(id));
    }

    // --- TESTE 5: DELETAR (DELETE) ---
    @Test
    @DisplayName("Deve deletar um livro com sucesso")
    void shouldDeleteBookSuccess() {
        // ARRANGE
        Long id = 1L;
        Book book = new Book();
        book.setId(id);

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        // ACT
        bookService.deleteBook(id);

        // ASSERT
        verify(bookRepository, times(1)).deleteById(id);
    }
}