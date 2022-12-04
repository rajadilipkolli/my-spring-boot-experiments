package com.example.jooq.r2dbc.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
        info = @Info(title = "spring-boot-jooq-r2dbc-sample", version = "v1"),
        servers = @Server(url = "/"))
public class SwaggerConfig {}
