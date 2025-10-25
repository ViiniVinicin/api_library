package br.com.management.api_library.config; // Seu pacote config

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Desabilita CSRF (comum e seguro para APIs stateless)
                .csrf(csrf -> csrf.disable())

                // 2. Define as regras de autorização (quem pode acessar o quê)
                .authorizeHttpRequests(authorize -> authorize
                        // --- Endpoints Públicos ---
                        .requestMatchers(HttpMethod.POST, "/library_api/users").permitAll()      // Permite o cadastro de novos usuários
                        .requestMatchers(HttpMethod.GET, "/library_api/books/**").permitAll()    // Permite buscar/ver livros sem login

                        // --- Endpoints da Estante Pessoal (Shelf) ---
                        // Qualquer usuário autenticado pode gerenciar SUA PRÓPRIA estante
                        .requestMatchers("/library_api/shelf/**").authenticated()

                        // --- Endpoints de Gerenciamento de Livros (Catálogo Geral) ---
                        // Apenas usuários com a ROLE "ADMIN" podem modificar o catálogo
                        .requestMatchers(HttpMethod.POST, "/library_api/books").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/library_api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/library_api/books/**").hasRole("ADMIN")

                        // --- Endpoints de Usuário ---
                        // Exemplo: Permitir que usuários autenticados vejam perfis (ajuste conforme necessário)
                        // Poderia ser mais restrito, ex: apenas ADMIN vê todos, ou usuário só vê o próprio perfil.
                        // Por enquanto, qualquer usuário logado pode acessar GET /users/**
                        .requestMatchers(HttpMethod.GET, "/library_api/users/**").authenticated()
                        // Futuramente: Adicionar PUT/DELETE para /users/** (provavelmente só para ADMIN ou o próprio usuário)

                        // --- Regra Padrão ---
                        // Qualquer outra requisição não listada acima exige autenticação
                        .anyRequest().authenticated()
                )

                // 3. Habilita a autenticação HTTP Basic (para usar usuário/senha no Postman, etc.)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}