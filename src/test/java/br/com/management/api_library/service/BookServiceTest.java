package br.com.management.api_library.service;

import br.com.management.api_library.dto.BookCreateDTO;
import br.com.management.api_library.exception.BookAlreadyExistsException;
import br.com.management.api_library.model.Book;
import br.com.management.api_library.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 1. Inicializa o Mockito
class BookServiceTest {

    @Mock // 2. Cria um "Dublê" do Repositório
    private BookRepository bookRepository;

    @InjectMocks // 3. Injeta o dublê dentro do Service real
    private BookService bookService;

    @Test
    @DisplayName("Deve lançar erro quando tentar criar livro com ISBN já existente")
    void shouldThrowExceptionWhenIsbnExists() {
        // --- CENÁRIO (Arrange) ---
        String isbnDuplicado = "978-1234567890";

        // Criamos o DTO de entrada
        BookCreateDTO dto = new BookCreateDTO(
                "Título Qualquer",
                "Autor",
                "Editora",
                "Genre",
                "Description",
                "en",
                isbnDuplicado,
                100
        );

        // Aqui está a mágica do Mockito:
        // "Quando o repositório for chamado buscando por este ISBN,
        // finja que encontrou um livro (retorne um Optional com um livro vazio)."
        when(bookRepository.findByIsbn(isbnDuplicado))
                .thenReturn(Optional.of(new Book()));

        // --- AÇÃO & VERIFICAÇÃO (Act & Assert) ---

        // O teste é: Esperamos que a chamada do método lance BookAlreadyExistsException
        assertThrows(BookAlreadyExistsException.class, () -> {
            bookService.createBook(dto);
        });

        // Verificação extra: Garante que o método save NUNCA foi chamado
        // (pois o erro deve acontecer antes de salvar)
        verify(bookRepository, never()).save(any(Book.class));
    }
}