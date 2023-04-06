package com.example.graphql.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "my-spring-graphql-webmvc", version = "v1"))
public class SwaggerConfig {}
