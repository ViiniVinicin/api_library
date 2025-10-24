package br.com.management.api_library.repository;
import br.com.management.api_library.model.Book;
import br.com.management.api_library.model.User;
import br.com.management.api_library.model.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookRepository extends JpaRepository <UserBook, Long>{
    List<UserBook> findByUser(User user);
    Optional<UserBook> findByUserAndBook(User user, Book book);

}