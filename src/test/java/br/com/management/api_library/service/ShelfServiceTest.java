package br.com.management.api_library.service;

import br.com.management.api_library.dto.BookResponseDTO;
import br.com.management.api_library.dto.ShelfItemRequestByIsbnDTO;
import br.com.management.api_library.dto.ShelfItemRequestDTO;
import br.com.management.api_library.dto.ShelfItemResponseDTO;
import br.com.management.api_library.exception.BookAlreadyExistsOnShelfException;
import br.com.management.api_library.exception.ResourceNotFoundException;
import br.com.management.api_library.exception.UnauthorizedShelfAccessException;
import br.com.management.api_library.model.Book;
import br.com.management.api_library.model.ReadingStatus;
import br.com.management.api_library.model.User;
import br.com.management.api_library.model.UserBook;
import br.com.management.api_library.repository.BookRepository;
import br.com.management.api_library.repository.UserBookRepository;
import br.com.management.api_library.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShelfServiceTest {

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookService bookService;

    @InjectMocks
    private ShelfService shelfService;

    // --- TESTES DE ADICIONAR POR ID (addBookToShelf) ---

    @Test
    @DisplayName("Deve adicionar livro à estante com sucesso")
    void shouldAddBookToShelfSuccess() {
        // ARRANGE
        String username = "erick";
        Long bookId = 1L;
        ShelfItemRequestDTO dto = new ShelfItemRequestDTO(ReadingStatus.READING, 5.0, "Ótimo", 100, true);

        User user = new User(); user.setUsername(username);
        Book book = new Book(); book.setId(bookId); book.setTitle("Livro Teste"); book.setAuthor("Autor Teste");

        UserBook savedUserBook = new UserBook();
        savedUserBook.setId(10L);
        savedUserBook.setUser(user);
        savedUserBook.setBook(book);
        savedUserBook.setReadingStatus(ReadingStatus.READING);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserAndBook(user, book)).thenReturn(Optional.empty()); // Garante que não existe
        when(userBookRepository.save(any(UserBook.class))).thenReturn(savedUserBook);

        // ACT
        ShelfItemResponseDTO response = shelfService.addBookToShelf(username, bookId, dto);

        // ASSERT
        assertNotNull(response);
        assertEquals(savedUserBook.getId(), response.userBookId());
        verify(userBookRepository, times(1)).save(any(UserBook.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar adicionar livro duplicado")
    void shouldThrowExceptionWhenBookAlreadyOnShelf() {
        // ARRANGE
        String username = "erick";
        Long bookId = 1L;
        ShelfItemRequestDTO dto = new ShelfItemRequestDTO(ReadingStatus.WANT_TO_READ, null, null, null, false);

        User user = new User();
        Book book = new Book();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        // Simula que JÁ EXISTE
        when(userBookRepository.findByUserAndBook(user, book)).thenReturn(Optional.of(new UserBook()));

        // ACT & ASSERT
        assertThrows(BookAlreadyExistsOnShelfException.class, () ->
                shelfService.addBookToShelf(username, bookId, dto));

        verify(userBookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção se o livro não existir no banco")
    void shouldThrowExceptionWhenBookNotFound() {
        String username = "erick";
        Long bookId = 99L;
        ShelfItemRequestDTO dto = new ShelfItemRequestDTO(ReadingStatus.WANT_TO_READ, null, null, null, false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                shelfService.addBookToShelf(username, bookId, dto));
    }

    // --- TESTES DE ADICIONAR POR ISBN (addBookToShelfByIsbn) ---

    @Test
    @DisplayName("Deve adicionar livro por ISBN com sucesso")
    void shouldAddBookToShelfByIsbnSuccess() {
        // ARRANGE
        String username = "erick";
        String isbn = "978123456";
        ShelfItemRequestByIsbnDTO isbnDto = new ShelfItemRequestByIsbnDTO(isbn, ReadingStatus.WANT_TO_READ, null, null, null, false);

        // Mock do retorno do BookService
        BookResponseDTO bookResponseDTO = new BookResponseDTO(1L, isbn,"Titulo", "Autor", "Publisher", "Gen", "Desc", "Lang", 100);

        User user = new User(); user.setUsername(username);
        Book book = new Book(); book.setId(1L); book.setTitle("Titulo"); book.setAuthor("Autor"); // Mesmos dados do DTO

        UserBook savedUserBook = new UserBook();
        savedUserBook.setId(50L);
        savedUserBook.setBook(book);
        savedUserBook.setUser(user);

        // 1. Simula o BookService achando/criando o livro
        when(bookService.findOrCreateBookByIsbn(isbn)).thenReturn(bookResponseDTO);

        // 2. Simulações internas do metodo addBookToShelf (que é chamado dentro do ByIsbn)
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserAndBook(user, book)).thenReturn(Optional.empty());
        when(userBookRepository.save(any(UserBook.class))).thenReturn(savedUserBook);

        // ACT
        ShelfItemResponseDTO response = shelfService.addBookToShelfByIsbn(username, isbnDto);

        // ASSERT
        assertNotNull(response);
        assertEquals(50L, response.userBookId());
        verify(bookService).findOrCreateBookByIsbn(isbn); // Garante que chamou o BookService
        verify(userBookRepository).save(any()); // Garante que salvou na estante
    }

    // --- TESTES DE LISTAGEM (getUserShelf) ---

    @Test
    @DisplayName("Deve listar estante do usuário paginada")
    void shouldGetUserShelfPaginated() {
        // ARRANGE
        String username = "erick";
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();

        Book book = new Book(); book.setTitle("Book"); book.setAuthor("Auth");
        UserBook ub1 = new UserBook(); ub1.setBook(book); ub1.setUser(user);

        Page<UserBook> page = new PageImpl<>(List.of(ub1));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userBookRepository.findByUser(user, pageable)).thenReturn(page);

        // ACT
        Page<ShelfItemResponseDTO> result = shelfService.getUserShelf(username, pageable);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Book", result.getContent().getFirst().title());
    }

    // --- TESTES DE ATUALIZAÇÃO (updateBookOnShelf) ---

    @Test
    @DisplayName("Deve atualizar item da estante com sucesso")
    void shouldUpdateBookOnShelfSuccess() {
        // ARRANGE
        String username = "erick";
        Long userBookId = 10L;
        ShelfItemRequestDTO dto = new ShelfItemRequestDTO(ReadingStatus.COMPLETED, 5.0, "Amei", null, null);

        User user = new User(); user.setUsername(username);
        Book book = new Book(); book.setId(1L); book.setTitle("T"); book.setAuthor("A");

        UserBook existingItem = new UserBook();
        existingItem.setId(userBookId);
        existingItem.setUser(user); // Dono correto
        existingItem.setBook(book);
        existingItem.setReadingStatus(ReadingStatus.READING);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(existingItem));
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        ShelfItemResponseDTO response = shelfService.updateBookOnShelf(username, userBookId, dto);

        // ASSERT
        assertEquals(ReadingStatus.COMPLETED, response.readingStatus());
        assertEquals("Amei", response.review());
    }

    @Test
    @DisplayName("Deve lançar erro de autorização se tentar atualizar item de outro usuário")
    void shouldThrowUnauthorizedWhenUpdatingOtherUsersItem() {
        // ARRANGE
        String username = "hacker";
        Long userBookId = 10L;
        ShelfItemRequestDTO dto = new ShelfItemRequestDTO(ReadingStatus.COMPLETED, null, null, null, null);

        User hacker = new User(); hacker.setUsername("hacker"); hacker.setId(2L);
        User owner = new User(); owner.setUsername("dono"); owner.setId(1L);

        UserBook existingItem = new UserBook();
        existingItem.setId(userBookId);
        existingItem.setUser(owner); // O item pertence ao "dono"

        when(userRepository.findByUsername("hacker")).thenReturn(Optional.of(hacker));
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(existingItem));

        // ACT & ASSERT
        assertThrows(UnauthorizedShelfAccessException.class, () ->
                shelfService.updateBookOnShelf(username, userBookId, dto));
    }

    // --- TESTES DE REMOÇÃO (removeBookFromShelf) ---

    @Test
    @DisplayName("Deve remover item da estante com sucesso")
    void shouldRemoveBookFromShelfSuccess() {
        // ARRANGE
        String username = "erick";
        Long userBookId = 10L;

        User user = new User(); user.setUsername(username);
        UserBook item = new UserBook(); item.setId(userBookId); item.setUser(user);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(item));

        // ACT
        shelfService.removeBookFromShelf(username, userBookId);

        // ASSERT
        verify(userBookRepository, times(1)).delete(item);
    }
}