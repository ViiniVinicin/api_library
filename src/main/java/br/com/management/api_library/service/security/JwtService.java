package br.com.management.api_library.service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import br.com.management.api_library.model.User; // Sua entidade User
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class JwtService {

    @Value("${api.security.token.secret}")
    private String secret;

    // GERA o token com base no usuário
    public String generateToken(UserDetails userDetails) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("api-library") // Quem emitiu o token
                    .withSubject(userDetails.getUsername()) // O dono do token (username)
                    .withExpiresAt(generateExpirationDate()) // Validade
                    .sign(algorithm); // Assina
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    // VALIDA o token e retorna o Username se for válido
    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("api-library")
                    .build()
                    .verify(token) // Verifica assinatura e validade
                    .getSubject(); // Retorna o username que estava dentro
        } catch (JWTVerificationException exception) {
            return ""; // Retorna vazio se o token for inválido ou expirado
        }
    }

    // Define que o token expira em 2 horas (ajuste conforme necessário)
    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}