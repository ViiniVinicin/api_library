package br.com.management.api_library.config;

import br.com.management.api_library.model.Role;
import br.com.management.api_library.model.User;
import br.com.management.api_library.repository.RoleRepository;
import br.com.management.api_library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
public class DatabaseSeeder {

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // 1. CRIA A ROLE "ROLE_USER"
            Role roleUser = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName("ROLE_USER");
                return roleRepository.save(newRole);
            });

            // 2. CRIA A ROLE "ROLE_ADMIN"
            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName("ROLE_ADMIN");
                return roleRepository.save(newRole);
            });

            // 3. CRIA O USUÁRIO ADMIN SE NÃO EXISTIR
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123"));
                admin.setFullName("Administrador do Sistema");
                admin.setEmail("admin@livraria.com"); // Ajuste se necessário

                // Dá poder total ao admin
                admin.setRoles(Collections.singleton(roleAdmin));

                userRepository.save(admin);
            }
        };
    }
}