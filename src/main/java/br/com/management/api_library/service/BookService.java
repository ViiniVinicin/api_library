package br.com.management.api_library.service;

import br.com.management.api_library.dto.*;
import br.com.management.api_library.exception.BookAlreadyExistsException;
import br.com.management.api_library.exception.ResourceNotFoundException;
import br.com.management.api_library.model.Book;
import br.com.management.api_library.repository.BookRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final IsbnService isbnService;

    public BookService(BookRepository bookRepository, IsbnService isbnService) {
        this.bookRepository = bookRepository;
        this.isbnService = isbnService;
    }

    public BookResponseDTO createBook(BookCreateDTO createDTO) {

        bookRepository.findByIsbn(createDTO.isbn())
                .ifPresent(existingBook -> {
                    throw new BookAlreadyExistsException("Erro: O livro com o ISBN: " + createDTO.isbn() + " já existe.");
                });

        Book newBook = new Book();
        mapDtoToEntity(newBook, createDTO);

        Book savedBook = bookRepository.save(newBook);
        return toResponseDTO(savedBook);
    }

    public BookResponseDTO getById(Long id) {
        return bookRepository.findById(id)
                // CORREÇÃO: Troque .get() por .orElseThrow(...)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com ID: " + id));
    }

    public Page<BookResponseDTO> getAllBooks(Pageable pageable) {

        return bookRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    public BookResponseDTO getBookByTitle(String title) {

        Book books = bookRepository.findByTitleIgnoringCase(title)
                .orElseThrow(() -> new ResourceNotFoundException("Erro: Livro não encontrado com o título: " + title));
        return toResponseDTO(books);
    }

    public Page<BookResponseDTO> getBooksByGenre(String genre, Pageable pageable) {

        Page<Book> books = bookRepository.findByGenreIgnoringCase(genre, pageable);

        if (books.isEmpty()) {
            throw new ResourceNotFoundException("Erro: Nenhum livro do gênero " + genre + " foi encontrado.");
        } else {
            return books.map(this::toResponseDTO);
        }
    }

    public List<BookResponseDTO> getByAuthor(String author) {
        List<Book> books = bookRepository.findAll();

        if (books.isEmpty()) {
            throw new ResourceNotFoundException("Erro: Nenhum livro do autor " + author + " foi encontrado.");
        } else {
            return books.stream()
                    .filter(b -> b.getAuthor().equalsIgnoreCase(author))
                    .map(this::toResponseDTO)
                    .toList();
        }
    }

    public List<BookResponseDTO> getByPublisher(String publisher) {
        List<Book> books = bookRepository.findAll();

        if (books.isEmpty()) {
            throw new ResourceNotFoundException("Erro: Nenhum livro da editora " + publisher + " foi encontrado.");
        } else {
            return books.stream()
                    .filter(b -> b.getPublisher().equalsIgnoreCase(publisher))
                    .map(this::toResponseDTO)
                    .toList();
        }
    }

    public BookResponseDTO updateBook(Long id, @Valid BookUpdateDTO updateDTO) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Erro: Livro não encontrado com o ID: " + id));

        mapDtoToEntity(existingBook, updateDTO);

        Book updatedBook = bookRepository.save(existingBook);
        return toResponseDTO(updatedBook);
    }

    public void deleteBook(Long id) {
        bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Erro: Livro não encontrado com o ID: " + id));

        bookRepository.deleteById(id);
    }

    // ISBN

    @Transactional // Leitura e potencial escrita, então usamos @Transactional
    public BookResponseDTO findOrCreateBookByIsbn(String isbn) {
        // 1. Verifica se o livro JÁ EXISTE no nosso banco local pelo ISBN
        Optional<Book> existingBookOpt = bookRepository.findByIsbn(isbn);

        if (existingBookOpt.isPresent()) {
            // 2. Se existe, apenas converte para DTO e retorna
            log.info("Livro com ISBN {} encontrado no banco local.", isbn);
            return toResponseDTO(existingBookOpt.get());
        } else {
            // 3. Se NÃO existe localmente, chama o IsbnService para buscar externamente
            log.info("Livro com ISBN {} não encontrado localmente. Buscando na API externa...", isbn);
            Optional<GoogleBookVolumeInfo> externalBookInfoOpt = isbnService.findBookInfoByIsbn(isbn);

            if (externalBookInfoOpt.isPresent()) {
                // 4. Se a API externa encontrou, converte os dados externos para nossa entidade Book
                GoogleBookVolumeInfo volumeInfo = externalBookInfoOpt.get();
                Book newBook = mapGoogleVolumeInfoToBook(isbn, volumeInfo); // Método auxiliar (ver abaixo)

                // 5. Salva o novo livro no nosso banco de dados
                Book savedBook = bookRepository.save(newBook);
                log.info("Livro com ISBN {} encontrado externamente e salvo localmente com ID {}.", isbn, savedBook.getId());

                // 6. Converte a entidade salva para DTO e retorna
                return toResponseDTO(savedBook);
            } else {
                // 7. Se nem a API externa encontrou, lança exceção
                log.warn("Livro com ISBN {} não encontrado em nenhuma fonte.", isbn);
                throw new ResourceNotFoundException("Livro não encontrado com o ISBN: " + isbn);
            }
        }
    }

    private Book mapGoogleVolumeInfoToBook(String isbn, GoogleBookVolumeInfo volumeInfo) {
        Book book = new Book();
        book.setIsbn(isbn); // O ISBN que usamos na busca
        book.setTitle(volumeInfo.title());

        String isbnEncontrado = null;
        if (volumeInfo.industryIdentifiers() != null) {
            // Tenta achar o ISBN_13 primeiro
            isbnEncontrado = volumeInfo.industryIdentifiers().stream()
                    .filter(id -> "ISBN_13".equals(id.type()))
                    .map(IndustryIdentifier::identifier)
                    .findFirst()
                    .orElse(null);

            // Se não achar ISBN_13, tenta o ISBN_10
            if (isbnEncontrado == null) {
                isbnEncontrado = volumeInfo.industryIdentifiers().stream()
                        .filter(id -> "ISBN_10".equals(id.type()))
                        .map(IndustryIdentifier::identifier)
                        .findFirst()
                        .orElse(null);
            }
        }

        // Junta a lista de autores em uma única string separada por vírgula
        if (volumeInfo.authors() != null && !volumeInfo.authors().isEmpty()) {
            book.setAuthor(String.join(", ", volumeInfo.authors()));
        } else {
            book.setAuthor("Autor Desconhecido"); // Valor padrão
        }

        book.setPublisher(volumeInfo.publisher() != null ? volumeInfo.publisher() : "Editora Desconhecida");
        book.setDescription(volumeInfo.description());

        // Pega a URL da capa (thumbnail) se existir
        if (volumeInfo.imageLinks() != null && volumeInfo.imageLinks().containsKey("thumbnail")) {
            book.setImageUrl(volumeInfo.imageLinks().get("thumbnail"));
        } else if (volumeInfo.imageLinks() != null && volumeInfo.imageLinks().containsKey("smallThumbnail")) {
            book.setImageUrl(volumeInfo.imageLinks().get("smallThumbnail")); // Tenta a menor
        }

        if (volumeInfo.categories() != null && !volumeInfo.categories().isEmpty()) {
            book.setGenre(volumeInfo.categories().get(0)); // Pega o primeiro item da lista de categorias
        }

        // Mapeia 'language' (Google) para 'language' (sua Entidade)
        book.setLanguage(volumeInfo.language());

        // Mapeia 'pageCount' (Google) para 'pages' (sua Entidade)
        book.setPages(volumeInfo.pageCount());

        return book;
    }

    private static final Logger log = LoggerFactory.getLogger(BookService.class);


    private BookResponseDTO toResponseDTO(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getGenre(),
                book.getDescription(),
                book.getLanguage(),
                book.getPages()
        );
    }

    private void mapDtoToEntity(Book book, BookCreateDTO createDTO) {
        book.setTitle(createDTO.title());
        book.setAuthor(createDTO.author());
        book.setPublisher(createDTO.publisher());
        book.setGenre(createDTO.genre());
        book.setDescription(createDTO.description());
        book.setLanguage(createDTO.language());
        book.setIsbn(createDTO.isbn());
        book.setPages(createDTO.pages());
    }

    private void mapDtoToEntity(Book book, BookUpdateDTO updateDTO) {
        book.setTitle(updateDTO.title());
        book.setAuthor(updateDTO.author());
        book.setPublisher(updateDTO.publisher());
        book.setGenre(updateDTO.genre());
        book.setDescription(updateDTO.description());
        book.setLanguage(updateDTO.language());
        book.setIsbn(updateDTO.isbn());
        book.setPages(updateDTO.pages());
    }

}
