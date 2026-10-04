package com.dieletech.backend.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.Components
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("DIELTECH API")
                .version("1.0.0")
                .description("API REST de la plataforma de aprendizaje virtual Dieletech")
                .contact(
                    Contact()
                        .name("Diego Betancur")
                        .email("betancurdiego123@gmail.com")
                )
        )
        .addSecurityItem(SecurityRequirement().addList("Bearer JWT"))
        .components(
            Components().addSecuritySchemes(
                "Bearer JWT",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Pega tu token JWT aqui (sin 'Bearer ')")
            )
        )
}
