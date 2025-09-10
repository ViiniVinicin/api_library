package br.com.management.api_library.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // REGRAS PÚBLICAS (qualquer um pode acessar)
                        .requestMatchers(HttpMethod.GET, "/library_api/books/**").permitAll() // Permite ver livros e um livro específico
                        .requestMatchers(HttpMethod.POST, "/library_api/users").permitAll() // PERMITE O REGISTRO DE NOVOS USUÁRIOS para qualquer um

                        // REGRAS DE USUÁRIO (precisa estar logado, qualquer role)
                        // Exemplo: um usuário logado pode ver o perfil de outro
                        .requestMatchers(HttpMethod.GET, "/library_api/users/**").hasAnyRole("USER", "ADMIN")

                        // REGRAS DE ADMIN (apenas quem tem a role 'ADMIN')
                        .requestMatchers(HttpMethod.POST, "/library_api/books").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/library_api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/library_api/books/**").hasRole("ADMIN")

                        // REGRA GERAL (qualquer outra requisição precisa de autenticação)
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // Seus outros beans (userDetailsService e passwordEncoder) permanecem os mesmos.
    /*@Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "USER") // O admin também é um usuário
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}