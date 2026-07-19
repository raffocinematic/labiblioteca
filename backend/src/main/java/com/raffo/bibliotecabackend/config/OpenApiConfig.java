package com.raffo.bibliotecabackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca Gestionale API")
                        .description("API REST per gestione libri, autenticazione e catalogo biblioteca")
                        .version("1.0.0"));
    }
}
