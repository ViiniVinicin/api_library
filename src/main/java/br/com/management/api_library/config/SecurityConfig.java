package br.com.management.api_library.config; // Seu pacote config

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authorize -> authorize
                        // 2. FALTOU ISSO: Liberar o endpoint de Login!
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                        // Seus outros endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/library_api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/library_api/books/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Seus endpoints protegidos (estão corretos)
                        .requestMatchers("/library_api/shelf/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/library_api/books").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/library_api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/library_api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/library_api/users/**").authenticated()

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