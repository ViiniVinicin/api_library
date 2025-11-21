package br.com.management.api_library.repository;

import br.com.management.api_library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByTitleIgnoringCase(String title);
    Page<Book> findByGenreIgnoringCase(String genre, Pageable pageable);
    Optional<Book> findByIsbn(String isbn);
}
