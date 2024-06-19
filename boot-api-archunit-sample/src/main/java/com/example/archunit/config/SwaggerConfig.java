package com.example.archunit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SwaggerConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("boot-api-archunit-sample")
                        .description("Client Service APIs")
                        .version("v1.0.0")
                        .contact(new Contact().name("Raja Kolli").email("rajakolli@gmail.com")))
                .servers(List.of(new Server().url("/")));
    }
}
