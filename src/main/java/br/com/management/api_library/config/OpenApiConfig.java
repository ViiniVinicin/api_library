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
// Definição visual do cadeado do JWT
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 1. Informações de Capa da Documentação
                .info(new Info()
                        .title("Library Management API")
                        .description("API REST para gerenciamento de biblioteca pessoal. \n" +
                                "Integração com Google Books, controle de leitura e segurança JWT.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Vinicius")
                                .email("vinicius@exemplo.com")
                        )
                )
                // 2. Aplica o cadeado de segurança em todos os endpoints por padrão
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}