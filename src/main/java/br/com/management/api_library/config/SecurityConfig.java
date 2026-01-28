package br.com.management.api_library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Desativa CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // 1. DOCUMENTAÇÃO (SWAGGER) - LIBERADO
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**"
                        ).permitAll()

                        // 2. ENDPOINTS PÚBLICOS (Qualquer um acessa)
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll() // Login
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll() // NOVO: Registro Público
                        .requestMatchers(HttpMethod.GET, "/library_api/books/**").permitAll() // Ver catálogo de livros

                        // 3. GESTÃO DE USUÁRIOS (ADMINISTRAÇÃO)
                        // Criar usuário pela rota administrativa agora é só ADMIN
                        .requestMatchers(HttpMethod.POST, "/library_api/users").hasRole("ADMIN")
                        // Listar todos os usuários agora é só ADMIN
                        .requestMatchers(HttpMethod.GET, "/library_api/users").hasRole("ADMIN")
                        // Deletar usuário -> Só ADMIN (Isso impede que usuários apaguem uns aos outros)
                        .requestMatchers(HttpMethod.DELETE, "/library_api/users/**").hasRole("ADMIN")
                        // Alterar usuário -> Somente admin pode alterar todos e cada um altera o seu
                        .requestMatchers(HttpMethod.PUT, "/library_api/users/**").authenticated()

                        // 4. PERFIL E BUSCA SOCIAL
                        // Qualquer usuário logado pode buscar ou ver perfil específico
                        .requestMatchers(HttpMethod.GET, "/library_api/users/**").authenticated()

                        // 5. ADMINISTRAÇÃO DE LIVROS (Apenas Admin altera o catálogo)
                        .requestMatchers(HttpMethod.POST, "/library_api/books").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/library_api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/library_api/books/**").hasRole("ADMIN")

                        // 6. MINHA ESTANTE (Pessoal e Protegido)
                        .requestMatchers("/library_api/shelf/**").authenticated()

                        // 7. QUALQUER OUTRA ROTA PRECISA DE LOGIN
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}