package br.com.management.api_library.service;

import br.com.management.api_library.dto.BookCreateDTO;
import br.com.management.api_library.dto.BookResponseDTO;
import br.com.management.api_library.dto.GoogleBookVolumeInfo;
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

import java.util.List;
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

    @Mock
    private IsbnService isbnService;

    // --- NOVOS TESTES: Lógica de Busca Híbrida (Local vs Google) ---

    @Test
    @DisplayName("Deve retornar livro do banco local se já existir (sem chamar Google)")
    void shouldReturnLocalBookIfItExists() {
        // ARRANGE
        String isbn = "978-LOCAL";
        Book localBook = new Book();
        localBook.setId(1L);
        localBook.setIsbn(isbn);
        localBook.setTitle("Livro Local");

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(localBook));

        // ACT
        BookResponseDTO response = bookService.findOrCreateBookByIsbn(isbn);

        // ASSERT
        assertEquals("Livro Local", response.title());
        verify(bookRepository, never()).save(any()); // Não deve salvar de novo
        verify(isbnService, never()).findBookInfoByIsbn(any()); // Não deve chamar API externa
    }

    @Test
    @DisplayName("Deve buscar na API externa e salvar quando não existir localmente")
    void shouldFetchFromGoogleAndSaveWhenNotLocal() {
        // ARRANGE
        String isbn = "978-GOOGLE";

        // 1. Não achou no banco local
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        // 2. Achou na API do Google
        // AQUI ESTÁ A CORREÇÃO: A ordem exata baseada no seu Record
        GoogleBookVolumeInfo googleInfo = new GoogleBookVolumeInfo(
                "Livro Google",                  // 1. title
                List.of("Autor Google"),         // 2. authors
                "Editora G",                     // 3. publisher
                "Descrição do livro",            // 4. description
                null,                            // 5. imageLinks (Map) - Pode ser null pois tem check no service
                null,                            // 6. categories (List)
                "PT",                            // 7. language
                100,                             // 8. pageCount (int)
                null                             // 9. industryIdentifiers (List)
        );

        when(isbnService.findBookInfoByIsbn(isbn)).thenReturn(Optional.of(googleInfo));

        // 3. Mock do salvamento
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(2L); // Simula o ID gerado pelo banco
            return b;
        });

        // ACT
        BookResponseDTO response = bookService.findOrCreateBookByIsbn(isbn);

        // ASSERT
        assertEquals("Livro Google", response.title());
        assertEquals("Autor Google", response.author());
        verify(isbnService).findBookInfoByIsbn(isbn); // Garante que chamou o Google
        verify(bookRepository).save(any(Book.class)); // Garante que salvou
    }

    @Test
    @DisplayName("Deve lançar erro se não existir nem local nem na API externa")
    void shouldThrowExceptionWhenNotFoundAnywhere() {
        // ARRANGE
        String isbn = "978-NADA";
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
        when(isbnService.findBookInfoByIsbn(isbn)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> bookService.findOrCreateBookByIsbn(isbn));
    }
}