package br.com.management.api_library.service;

import br.com.management.api_library.dto.ShelfItemRequestDTO;
import br.com.management.api_library.dto.ShelfItemResponseDTO;
import br.com.management.api_library.exception.BookAlreadyExistsOnShelfException;
import br.com.management.api_library.exception.ResourceNotFoundException;
import br.com.management.api_library.exception.ShelfItemNotFoundException;
import br.com.management.api_library.exception.UnauthorizedShelfAccessException;
import br.com.management.api_library.model.Book;
import br.com.management.api_library.model.ReadingStatus; // Importe seu Enum
import br.com.management.api_library.model.User;
import br.com.management.api_library.model.UserBook;
import br.com.management.api_library.repository.BookRepository;
import br.com.management.api_library.repository.UserBookRepository;
import br.com.management.api_library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShelfService {

    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    // @Autowired é opcional em construtores a partir de certas versões do Spring
    public ShelfService(UserBookRepository userBookRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.userBookRepository = userBookRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public ShelfItemResponseDTO addBookToShelf(String username, Long bookId, ShelfItemRequestDTO requestDTO) {
        User user = findUserByUsername(username);
        Book book = findBookById(bookId);

        userBookRepository.findByUserAndBook(user, book).ifPresent(ub -> {
            throw new BookAlreadyExistsOnShelfException("Livro com ID " + bookId + " já está na sua estante.");
        });

        UserBook newUserBook = new UserBook();
        newUserBook.setUser(user);
        newUserBook.setBook(book);

        // Define status inicial ou o que veio no DTO
        newUserBook.setReadingStatus(requestDTO.readingStatus() != null ? requestDTO.readingStatus() : ReadingStatus.WANT_TO_READ);

        // Define outros campos, tratando nulos se necessário
        newUserBook.setRating(requestDTO.rating()!= null ? requestDTO.rating() : 0.0);
        newUserBook.setReview(requestDTO.review());
        newUserBook.setCurrentPage(requestDTO.currentPage() != null ? requestDTO.currentPage() : 0);
        newUserBook.setFavorite(requestDTO.isFavorite() != null ? requestDTO.isFavorite() : false);

        UserBook savedUserBook = userBookRepository.save(newUserBook);
        return toResponseDTO(savedUserBook);
    }

    @Transactional(readOnly = true)
    public List<ShelfItemResponseDTO> getUserShelf(String username) {
        User user = findUserByUsername(username);
        List<UserBook> shelfItems = userBookRepository.findByUser(user); // Assumindo que este método existe no repo
        return shelfItems.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShelfItemResponseDTO updateBookOnShelf(String username, Long userBookId, ShelfItemRequestDTO requestDTO) {
        User user = findUserByUsername(username);
        UserBook userBook = findUserBookById(userBookId);

        // Verifica se o item pertence ao usuário logado
        if (!userBook.getUser().equals(user)) {
            throw new UnauthorizedShelfAccessException("Você não tem permissão para modificar este item da estante.");
        }

        // Atualiza apenas os campos fornecidos no DTO (trate nulos se a intenção for PATCH)
        if (requestDTO.readingStatus() != null) {
            userBook.setReadingStatus(requestDTO.readingStatus());
        }
        if (requestDTO.rating() != null) { // Permite setar null para remover avaliação? Decida a regra.
            userBook.setRating(requestDTO.rating());
        }
        if (requestDTO.review() != null) {
            userBook.setReview(requestDTO.review());
        }
        if (requestDTO.currentPage() != null) {
            userBook.setCurrentPage(requestDTO.currentPage());
        }
        if (requestDTO.isFavorite() != null) {
            userBook.setFavorite(requestDTO.isFavorite());
        }

        UserBook updatedUserBook = userBookRepository.save(userBook);
        return toResponseDTO(updatedUserBook);
    }

    @Transactional
    public void removeBookFromShelf(String username, Long userBookId) {
        User user = findUserByUsername(username);
        UserBook userBook = findUserBookById(userBookId);

        // Verifica a propriedade
        if (!userBook.getUser().equals(user)) {
            throw new UnauthorizedShelfAccessException("Você não tem permissão para remover este item da estante.");
        }

        userBookRepository.delete(userBook);
    }

    // --- Métodos Auxiliares ---

    private User findUserByUsername(String username) {
        // Garanta que findByUsername existe no UserRepository
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário '" + username + "' não encontrado."));
    }

    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Livro com ID " + bookId + " não encontrado."));
    }

    private UserBook findUserBookById(Long userBookId) {
        return userBookRepository.findById(userBookId)
                .orElseThrow(() -> new ShelfItemNotFoundException("Item da estante não encontrado com o ID: " + userBookId));
    }

    // Método de conversão para DTO de resposta
    private ShelfItemResponseDTO toResponseDTO(UserBook item) {
        // Garanta que Book tenha getTitle() e getAuthor()
        return new ShelfItemResponseDTO(
                item.getId(),
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getBook().getAuthor(),
                item.getReadingStatus(),
                item.getRating(),
                item.getReview(),
                item.isFavorite(),
                item.getCurrentPage()
        );
    }
}