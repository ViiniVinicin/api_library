package br.com.management.api_library;

import br.com.management.api_library.model.Role;
import br.com.management.api_library.model.User;
import br.com.management.api_library.repository.RoleRepository;
import br.com.management.api_library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
public class ApiLibraryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiLibraryApplication.class, args);
	}

    @Bean
    @Profile("!test")
    CommandLineRunner initDatabase(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Cria as ROLES se não existirem
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName("ROLE_ADMIN");
                return roleRepository.save(newRole);
            });

            Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName("ROLE_USER");
                return roleRepository.save(newRole);
            });

            // Cria o usuário ADMIN se não existir
            if (userRepository.findByUsername("admin").isEmpty()) {
                System.out.println("Criando usuário ADMIN padrão...");
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setFullName("Administrador do Sistema");
                adminUser.setEmail("admin@api_library.com");
                // CRIPTOGRAFA A SENHA
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                // ASSOCIA AS ROLES ADMIN e USER
                adminUser.setRoles(Set.of(adminRole, userRole));

                userRepository.save(adminUser);
                System.out.println("Usuário ADMIN criado com sucesso!");
            }
        };
    }

}
