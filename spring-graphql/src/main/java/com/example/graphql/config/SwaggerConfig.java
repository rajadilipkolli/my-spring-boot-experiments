package com.example.graphql.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {

  @Bean
  public GroupedOpenApi publicApi() {
      return GroupedOpenApi.builder()
              .group("spring-graphql")
              .packagesToScan("com.example.graphql.controller")
              .build();
  }

  @Bean
  public OpenAPI springShopOpenAPI() {
      return new OpenAPI()
              .info(new Info().title("SpringGraphQL API")
              .description("Spring graphql sample application")
              .version("v0.0.1")
              .license(new License().name("Apache 2.0").url("http://springdoc.org")))
              .externalDocs(new ExternalDocumentation()
              .description("SpringGraphQL Wiki Documentation")
              .url("https://springshop.wiki.github.org/docs"));
  }

}
