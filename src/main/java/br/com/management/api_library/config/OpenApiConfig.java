package br.com.management.api_library.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth", // Nome do esquema de segurança
        type = SecuritySchemeType.HTTP, // Tipo HTTP
        bearerFormat = "JWT", // Formato do token
        scheme = "bearer" // O prefixo (Bearer)
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Library")
                        .description("API de Livraria Pessoal com Spring Security e JWT")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Seu Nome")
                                .email("seu.email@exemplo.com")
                        )
                )
                // Adiciona a exigência de segurança globalmente
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}