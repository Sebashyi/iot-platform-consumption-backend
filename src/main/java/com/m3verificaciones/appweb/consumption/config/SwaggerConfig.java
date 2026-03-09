package com.m3verificaciones.appweb.consumption.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Verificaciones M3 - Consumos")
                        .version("1.0")
                        .description("Documentación de la API para el sistema de m3verificaciones"));
    }
}

