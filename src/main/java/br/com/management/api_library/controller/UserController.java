package br.com.management.api_library.controller;

import br.com.management.api_library.dto.UserCreateDTO;
import br.com.management.api_library.dto.UserResponseDTO;
import br.com.management.api_library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/library_api/users")
// 1. Define o nome do grupo na barra lateral do Swagger
@Tag(name = "Usuários", description = "Endpoints para criar, alterar, excluir e buscar usuários")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // 2. Descreve o que o método faz
    @Operation(summary = "Cadastrar novo usuário", description = "Cria um usuário na base de dados com as roles padrão.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "422", description = "Erro de validação nos dados (ex: email duplicado)")
    })
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserResponseDTO createdUser = userService.createUser(userCreateDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.id())
                .toUri();
        return ResponseEntity.created(location).body(createdUser);
    }

    @GetMapping
    @Operation(summary = "Listar todos", description = "Retorna a lista completa de usuários cadastrados.")
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        List<UserResponseDTO> usersDTO = userService.getAllUsers();
        return ResponseEntity.ok(usersDTO);
    }

    @GetMapping("/search/by-fullName")
    @Operation(summary = "Busca Inteligente por Nome", description = "Pesquisa usuários por partes do nome, ignorando acentos e maiúsculas.")
    public ResponseEntity<List<UserResponseDTO>> findByFullName(@RequestParam("fullName") String fullName) {
        List<UserResponseDTO> usersDTO = userService.searchByTerm(fullName);
        return ResponseEntity.ok(usersDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente pelo ID.")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserResponseDTO updatedUser = userService.updateUser(id, userCreateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário", description = "Remove permanentemente um usuário do sistema.")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}