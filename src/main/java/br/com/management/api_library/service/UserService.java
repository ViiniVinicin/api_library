package br.com.management.api_library.service;

import br.com.management.api_library.dto.UserCreateDTO;
import br.com.management.api_library.dto.UserResponseDTO;
import br.com.management.api_library.exception.*;
import br.com.management.api_library.model.Role;
import br.com.management.api_library.model.User;
import br.com.management.api_library.model.UserBook;
import br.com.management.api_library.repository.RoleRepository;
import br.com.management.api_library.repository.UserBookRepository;
import br.com.management.api_library.repository.UserRepository;
import jakarta.transaction.Transactional;
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

import static br.com.management.api_library.exception.GlobalHandlerException.log;

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
                .orElseThrow(() -> new UserNotFoundException("Usuário " + username + " não encontrado"));

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

        userRepository.findByUsernameIgnoreCase(createDTO.getUsername())
                .ifPresent(user -> {
                    log.warn("--- LANÇANDO UsernameAlreadyExistsException PARA: {} ---", createDTO.getUsername());
                    throw new UsernameAlreadyExistsException("Erro: O Username '" + createDTO.getUsername() + "' já está cadastrado.");
                });

        userRepository.findByEmailIgnoreCase(createDTO.getEmail())
                .ifPresent(user -> {
                    log.warn("--- LANÇANDO EmailAlreadyExistsException PARA: {} ---", createDTO.getEmail());
                    throw new EmailAlreadyExistsException("Erro: O e-mail '" + createDTO.getEmail() + "' já está cadastrado.");
                });

        User newUser = new User();
        newUser.setUsername(createDTO.getUsername());
        newUser.setEmail(createDTO.getEmail());
        newUser.setFullName(createDTO.getFullName());

        // 1. Criptografa a senha
        newUser.setPassword(passwordEncoder.encode(createDTO.getPassword()));

        // 2. Busca a role "USER" no banco de dados
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Erro: Role padrão não encontrada."));

        // 3. Atribui a role padrão ao novo usuário
        newUser.setRoles(Collections.singleton(userRole));

        return toResponseDTO(userRepository.save(newUser));
    }

    public List<UserResponseDTO> getAllUsers() {

        List<User> users = userRepository.findAll();

        return userRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public UserResponseDTO getByFullName(String fullName) {
        User user = userRepository.findByFullName(fullName)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com o nome: " + fullName));
        return toResponseDTO(user);
    }

    public UserResponseDTO updateUser(Long id, @Valid UserCreateDTO updateDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com o ID: " + id));

        mapDtoToEntity(existingUser, updateDTO);

        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return toResponseDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com o ID: " + id));

        userRepository.deleteById(id);
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