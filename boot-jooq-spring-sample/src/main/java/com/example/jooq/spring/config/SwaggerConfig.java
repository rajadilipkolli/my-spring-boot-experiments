package com.example.jooq.spring.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SwaggerConfig {

    /** Defines API metadata with a server URL relative to the current host. */
    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("boot-jooq-spring-sample")
                        .description("Sample application demonstrating jOOQ with Spring and jooq-spring DAO")
                        .version("v1.0.0"))
                .servers(List.of(new Server().url("/")));
    }
}
