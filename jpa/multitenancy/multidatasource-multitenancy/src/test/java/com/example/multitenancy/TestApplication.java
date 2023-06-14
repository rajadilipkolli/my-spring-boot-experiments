package com.example.multitenancy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestApplication {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> primaryPostgresContainer(
            DynamicPropertyRegistry dynamicPropertyRegistry) {
        PostgreSQLContainer<?> postgreSQLContainer =
                new PostgreSQLContainer<>("postgres:15.3-alpine");
        dynamicPropertyRegistry.add("datasource.primary.url", postgreSQLContainer::getJdbcUrl);
        dynamicPropertyRegistry.add(
                "datasource.primary.username", postgreSQLContainer::getUsername);
        dynamicPropertyRegistry.add(
                "datasource.primary.password", postgreSQLContainer::getPassword);
        return postgreSQLContainer;
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> secondaryPostgresContainer(
            DynamicPropertyRegistry dynamicPropertyRegistry) {
        PostgreSQLContainer<?> postgreSQLContainer =
                new PostgreSQLContainer<>("postgres:15.3-alpine");
        dynamicPropertyRegistry.add("datasource.secondary.url", postgreSQLContainer::getJdbcUrl);
        dynamicPropertyRegistry.add(
                "datasource.secondary.username", postgreSQLContainer::getUsername);
        dynamicPropertyRegistry.add(
                "datasource.secondary.password", postgreSQLContainer::getPassword);
        return postgreSQLContainer;
    }

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(TestApplication.class).run(args);
    }
}
