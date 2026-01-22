package br.com.management.api_library.service;

import br.com.management.api_library.dto.UserCreateDTO;
import br.com.management.api_library.dto.UserResponseDTO;
import br.com.management.api_library.exception.UsernameAlreadyExistsException;
import br.com.management.api_library.model.Role;
import br.com.management.api_library.model.User;
import br.com.management.api_library.repository.RoleRepository;
import br.com.management.api_library.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void deveLancarErroQuandoUsernameJaExiste() {
        // Arrange
        UserCreateDTO dto = new UserCreateDTO("viini", "123", "vinicius@email.com", "Erick Vinicius");
        when(userRepository.findByUsernameIgnoreCase("viini")).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.createUser(dto));

        // Garante que NUNCA tentou salvar
        verify(userRepository, never()).save(any());
    }

    @Test
    void deveCriarUsuarioComSucesso() {
        // Arrange
        UserCreateDTO dto = new UserCreateDTO("novo", "senha123", "novo@email.com", "Novo User");
        Role roleUser = new Role();
        roleUser.setName("ROLE_USER");

        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("senha123")).thenReturn("senhaCriptografada");

        // Mock do save retornando o usuário salvo
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        UserResponseDTO response = userService.createUser(dto);

        // Assert
        assertNotNull(response);
        assertEquals("novo", response.username());
        verify(passwordEncoder).encode("senha123"); // Garante que criptografou
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveCarregarUserDetailsComSucesso() {
        // Arrange
        String username = "vinicius";
        User user = new User();
        user.setUsername(username);
        user.setPassword("hashedPwd");

        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(java.util.Collections.singleton(role));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("hashedPwd", userDetails.getPassword());

        // Verifica se a role foi convertida corretamente para Authority
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void deveLancarErroAoCarregarUsuarioInexistente() {
        when(userRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("inexistente"));
    }
}