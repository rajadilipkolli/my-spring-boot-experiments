package com.example.multipledatasources.common;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration class that manages MySQL and PostgreSQL containers for integration testing.
 * This class is responsible for starting the containers, registering their connection properties,
 * and ensuring proper cleanup on shutdown.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration {

    private MySQLContainer<?> mySQLContainer;
    private PostgreSQLContainer<?> postgreSQLContainer;

    @Bean
    MySQLContainer<?> mySQLContainer() {
        mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql").withTag("9.1"));
        mySQLContainer.start();
        return mySQLContainer;
    }

    @Bean
    PostgreSQLContainer<?> postgreSQLContainer() {
        postgreSQLContainer =
                new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("17.2-alpine"));
        postgreSQLContainer.start();
        return postgreSQLContainer;
    }

    @Bean
    public DynamicPropertyRegistrar databaseProperties(
            MySQLContainer<?> mySQLContainer, PostgreSQLContainer<?> postgreSQLContainer) {
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

    @PreDestroy
    public void cleanup() {
        if (mySQLContainer != null && mySQLContainer.isRunning()) {
            mySQLContainer.stop();
        }
        if (postgreSQLContainer != null && postgreSQLContainer.isRunning()) {
            postgreSQLContainer.stop();
        }
    }
}
