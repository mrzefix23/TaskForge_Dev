package com.taskforge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration de la documentation OpenAPI (Swagger) pour l'application TaskForge.
 * Cette classe définit les métadonnées de l'API et la configuration de sécurité JWT.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Crée et configure le bean OpenAPI.
     * Définit les informations générales de l'API (titre, description, version, contact, licence)
     * et configure le schéma de sécurité pour l'authentification JWT (Bearer Token).
     *
     * @return Une instance d'OpenAPI configurée pour TaskForge.
     */
    @Bean
    public OpenAPI taskForgeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskForge API")
                        .description("API de gestion de projets et de tâches")
                        .version("v0.1.0")
                        .contact(new Contact()
                                .name("TaskForge Team")
                                .url("https://github.com/mrzefix23/TaskForge_Dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Entrez votre token JWT (sans 'Bearer ')")));
    }
}