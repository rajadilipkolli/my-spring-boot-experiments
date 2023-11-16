package com.example.mongoes.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "spring-boot-mongodb-elasticsearch", version = "v1"))
public class SwaggerConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().addServersItem(new Server().url("/"));
    }
}
