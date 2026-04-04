package com.example.multipledatasources.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration class that manages MySQL and PostgreSQL containers for integration testing.
 * This class is responsible for starting the containers, registering their connection properties,
 * and ensuring proper cleanup on shutdown.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration {

    @Bean
    MySQLContainer mySQLContainer() {
        return new MySQLContainer(DockerImageName.parse("mysql").withTag("9.5"));
    }

    @Bean
    PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres").withTag("18.3-alpine"));
    }

    @Bean
    DynamicPropertyRegistrar databaseProperties(
            MySQLContainer mySQLContainer, PostgreSQLContainer postgreSQLContainer) {
        return (properties) -> {
            // Connect our Spring application to our Testcontainers instances
            properties.add("app.datasource.cardholder.url", mySQLContainer::getJdbcUrl);
            properties.add("app.datasource.cardholder.username", mySQLContainer::getUsername);
            properties.add("app.datasource.cardholder.password", mySQLContainer::getPassword);
            properties.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
            properties.add("spring.datasource.username", postgreSQLContainer::getUsername);
            properties.add("spring.datasource.password", postgreSQLContainer::getPassword);
        };
    }
}
