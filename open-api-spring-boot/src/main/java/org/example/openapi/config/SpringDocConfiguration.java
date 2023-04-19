package org.example.openapi.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
        info = @Info(title = "open-api-spring-boot-contract-first", version = "v1"),
        servers = @Server(url = "/"))
public class SpringDocConfiguration {

}
