package br.com.management.api_library.controller;

import br.com.management.api_library.dto.LoginRequestDTO;
import br.com.management.api_library.dto.LoginResponseDTO;
import br.com.management.api_library.dto.UserCreateDTO;
import br.com.management.api_library.dto.UserResponseDTO;
import br.com.management.api_library.service.UserService;
import br.com.management.api_library.service.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para Login e Registro (Sign Up)")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Realizar Login", description = "Autentica o usuário com username e senha e retorna um Token JWT Bearer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (usuário ou senha incorretos)")
    })
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO data) {

        var usernamePassword = new UsernamePasswordAuthenticationToken(data.username(), data.password());

        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar-se (Sign Up)", description = "Cria uma nova conta de usuário comum no sistema (Público).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos ou usuário já existente")
    })
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        // Reutilizamos a lógica de criação do service
        UserResponseDTO createdUser = userService.createUser(userCreateDTO);

        // Cria a URI apontando para o perfil do usuário (/library_api/users/{id})
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/library_api/users/{id}")
                .buildAndExpand(createdUser.id())
                .toUri();

        return ResponseEntity.created(location).body(createdUser);
    }
}