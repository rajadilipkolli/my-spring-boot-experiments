package com.example.jndi.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class ContainersConfig {

    private static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("17.4-alpine"));

    static {
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("application.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("application.datasource.username", postgreSQLContainer::getUsername);
        registry.add("application.datasource.password", postgreSQLContainer::getPassword);
    }
}
