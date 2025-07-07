package com.example.plugin.strategyplugin.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "spring-boot-strategy-plugin", version = "v1"),
        servers = @Server(url = "/"))
class SwaggerConfig {}
