package br.com.management.api_library.service;

import br.com.management.api_library.dto.BookCreateDTO;
import br.com.management.api_library.dto.BookResponseDTO;
import br.com.management.api_library.dto.BookUpdateDTO;
import br.com.management.api_library.exception.BookAlreadyExistsException;
import br.com.management.api_library.exception.ResourceNotFoundException;
import br.com.management.api_library.model.Book;
import br.com.management.api_library.repository.BookRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponseDTO> getAllBooks() {

        List<Book> books = bookRepository.findAll();

        return bookRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public BookResponseDTO getBookByTitle(String bookTitle) {

        Book books = bookRepository.findByTitleIgnoringCase(bookTitle)
                .orElseThrow(() -> new ResourceNotFoundException("Erro: Livro não encontrado com o título: " + bookTitle));
        return toResponseDTO(books);
    }

    public List<BookResponseDTO> getBooksByGenre(String genre) {

        List<Book> books = bookRepository.findByGenreIgnoringCase(genre);

        if (books.isEmpty()) {
            throw new ResourceNotFoundException("Erro: Nenhum livro do gênero " + genre + " foi encontrado.");
        } else {
            return books.stream()
                    .filter(b -> b .getGenre().equalsIgnoreCase(genre))
                    .map(this::toResponseDTO)
                    .toList();
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

    public BookResponseDTO createBook(BookCreateDTO createDTO) {

        bookRepository.findByTitleIgnoringCase(createDTO.bookTitle())
                .ifPresent(book -> {
                    throw new BookAlreadyExistsException("Erro: O livro com o título: " + createDTO.bookTitle() + " já existe.");
                });

        Book newBook = new Book();
        mapDtoToEntity(newBook, createDTO);

        Book savedBook = bookRepository.save(newBook);
        return toResponseDTO(savedBook);
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

    private BookResponseDTO toResponseDTO(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getBookTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getGenre(),
                book.getDescription(),
                book.getLanguage(),
                book.getPages(),
                book.getPublicationYear(),
                book.getAvailableCopies()
        );
    }

    private void mapDtoToEntity(Book book, BookCreateDTO createDTO) {
        book.setBookTitle(createDTO.bookTitle());
        book.setAuthor(createDTO.author());
        book.setPublisher(createDTO.publisher());
        book.setGenre(createDTO.genre());
        book.setDescription(createDTO.description());
        book.setLanguage(createDTO.language());
        book.setIsbn(createDTO.isbn());
        book.setPages(createDTO.pages());
        book.setPublicationYear(createDTO.publicationYear());
        book.setAvailableCopies(createDTO.availableCopies());
        book.setAvailable(createDTO.availableCopies() > 0);
    }

    private void mapDtoToEntity(Book book, BookUpdateDTO updateDTO) {
        book.setBookTitle(updateDTO.bookTitle());
        book.setAuthor(updateDTO.author());
        book.setPublisher(updateDTO.publisher());
        book.setGenre(updateDTO.genre());
        book.setDescription(updateDTO.description());
        book.setLanguage(updateDTO.language());
        book.setIsbn(updateDTO.isbn());
        book.setPages(updateDTO.pages());
        book.setPublicationYear(updateDTO.publicationYear());
        book.setAvailableCopies(updateDTO.availableCopies());
        book.setAvailable(updateDTO.availableCopies() > 0);
    }

}
