package br.com.management.api_library.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class User {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
}
