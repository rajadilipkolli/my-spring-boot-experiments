package com.example.graphql.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("spring-boot-graphql-webflux")
                .packagesToScan("com.example.graphql.controller")
                .build();
    }

    @Bean
    public OpenAPI springGraphQLOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SpringGraphQL API")
                        .description("Spring graphql sample application")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("SpringGraphQL Wiki Documentation")
                        .url("https://springshop.wiki.github.org/docs"))
                .servers(List.of(new Server().url("/").description("SpringGraphQL")));
    }
}
