package br.com.management.api_library.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
// 1. Configura o "Cadeado" (Definição Técnica)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
// 2. Configurações Gerais do Documento
@OpenAPIDefinition(
        info = @Info(
                title = "Library Management API",
                version = "1.0",
                description = "API REST para gerenciamento de biblioteca pessoal. Integração com Google Books, controle de leitura e segurança JWT.",
                contact = @Contact(
                        name = "Erick Vinícius de Oliveira Nascimento",
                        email = "erickvini96@gmail.com"
                )
        ),
        // 3. Aplica o cadeado em TODAS as rotas globalmente
        security = @SecurityRequirement(name = "bearerAuth"),

        // 4. Definição de Ordem das Pastas (Tags)
        tags = {
                @Tag(name = "Autenticação", description = "Login e Tokens"),
                @Tag(name = "Minha Estante", description = "Gerenciamento da leitura pessoal"),
                @Tag(name = "Usuários", description = "Gestão de pessoas"),
                @Tag(name = "Catálogo de Livros", description = "Gestão do acervo global")
        }
)
public class OpenApiConfig {
}