package br.com.management.api_library.service;

import br.com.management.api_library.dto.UserCreateDTO;
import br.com.management.api_library.dto.UserResponseDTO;
import br.com.management.api_library.model.Role;
import br.com.management.api_library.model.User;
import br.com.management.api_library.repository.RoleRepository;
import br.com.management.api_library.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles())
        );
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    public UserResponseDTO createUser(UserCreateDTO createDTO) {
        User newUser = new User();
        newUser.setUsername(createDTO.getUsername());
        newUser.setEmail(createDTO.getEmail());
        newUser.setFullName(createDTO.getFullName());

        // 1. Criptografa a senha
        newUser.setPassword(passwordEncoder.encode(createDTO.getPassword()));

        // 2. Busca a role "USER" no banco de dados
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Erro: Role padrão não encontrada."));

        // 3. Atribui a role padrão ao novo usuário
        newUser.setRoles(Collections.singleton(userRole));

        return toResponseDTO(userRepository.save(newUser));
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário не encontrado com o ID: " + id)); // Futuramente, use uma exceção customizada.
    }

    public UserResponseDTO getByFullName(String fullName) {
        User user = userRepository.findByFullName(fullName)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o nome completo: " + fullName)); // Futuramente, use uma exceção customizada.
        return toResponseDTO(user);
    }

    public UserResponseDTO updateUser(Long id, @Valid UserCreateDTO updateDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id)); // Futuramente, use uma exceção customizada.

        mapDtoToEntity(existingUser, updateDTO);

        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return toResponseDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id)); // Futuramente, use uma exceção customizada.

        userRepository.delete(user);
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName());
    }

    private void mapDtoToEntity(User user, @Valid UserCreateDTO updateDTO) {
        user.setUsername(updateDTO.getUsername());
        user.setEmail(updateDTO.getEmail());
        user.setFullName(updateDTO.getFullName());
    }


}