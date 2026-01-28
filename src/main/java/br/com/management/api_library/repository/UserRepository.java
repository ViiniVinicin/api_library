package br.com.management.api_library.repository;

import br.com.management.api_library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByFullName(String fullName);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsernameIgnoreCase(String username);

    @Query(value = """
            SELECT * FROM table_users u 
            WHERE unaccent(u.full_name) ILIKE unaccent(concat('%', :term, '%'))
            """, nativeQuery = true)
    List<User> searchByFullNamePolyglot(@Param("term") String term);
}
