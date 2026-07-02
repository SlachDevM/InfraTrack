package com.infratrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI infratrackOpenApi(
            @Value("${spring.application.name:InfraTrack}") String applicationName) {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .description("""
                                REST API for InfraTrack operational asset management.
                                Authenticated endpoints require a JWT bearer token from POST /api/auth/login.
                                Paginated collections accept optional page and size query parameters (defaults: page=0, size=20, max size=100).
                                Business errors are returned as plain-text response bodies with appropriate HTTP status codes.
                                """)
                        .version("2.0.1")
                        .contact(new Contact().name("InfraTrack"))
                        .license(new License().name("Proprietary")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT obtained from POST /api/auth/login")));
    }
}
