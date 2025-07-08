package com.example.multitenancy.db.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "multitenancy-db", version = "v1"), servers = @Server(url = "/"))
class SwaggerConfig {}
