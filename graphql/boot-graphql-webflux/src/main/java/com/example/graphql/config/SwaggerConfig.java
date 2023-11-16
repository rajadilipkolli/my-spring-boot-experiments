package com.example.graphql.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
        info =
                @Info(
                        title = "boot-graphql-webflux",
                        description = "Spring graphql sample application",
                        version = "v1",
                        license = @License(name = "Apache 2.0", url = "http://springdoc.org")),
        servers = @Server(url = "/", description = "SpringGraphQL"),
        externalDocs =
                @ExternalDocumentation(
                        description = "SpringGraphQL Wiki Documentation",
                        url =
                                "https://rajadilipkolli.gitbook.io/my-spring-boot-experiments/graphql/boot-graphql-webflux"))
public class SwaggerConfig {

    @Bean
    GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("spring-boot")
                .packagesToScan("com.example.graphql.controller")
                .build();
    }
}
