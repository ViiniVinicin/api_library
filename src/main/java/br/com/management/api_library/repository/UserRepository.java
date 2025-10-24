package br.com.management.api_library.repository;

import br.com.management.api_library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByFullName(String fullName);
    Optional<User> findByEmailIgnoreCase(String email);
}
