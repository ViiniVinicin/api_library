package br.com.management.api_library.service;

import br.com.management.api_library.dto.UserCreateDTO;
import br.com.management.api_library.model.User;
import br.com.management.api_library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Cria um novo usuário no banco de dados.
     */
    public User createUser(UserCreateDTO createDTO) {
        // TODO: AQUI É ONDE A CRIPTOGRAFIA DE SENHA DEVE ACONTECER!
        // Por enquanto, vamos salvar como texto puro, mas o correto é usar um
        // PasswordEncoder do Spring Security. Ex: passwordEncoder.encode(createDTO.getPassword())

        User newUser = new User();
        newUser.setUsername(createDTO.getUsername());
        newUser.setPassword(createDTO.getPassword()); // AVISO: Inseguro! Apenas para fins de desenvolvimento.
        newUser.setEmail(createDTO.getEmail());
        newUser.setFullName(createDTO.getFullName());

        return userRepository.save(newUser);
    }

    /**
     * Retorna uma lista de todos os usuários.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Busca um usuário pelo seu ID.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário не encontrado com o ID: " + id)); // Futuramente, use uma exceção customizada.
    }
}