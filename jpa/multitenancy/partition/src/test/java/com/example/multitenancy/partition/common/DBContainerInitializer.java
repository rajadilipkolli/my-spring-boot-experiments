package com.example.multitenancy.partition.common;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class DBContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> sqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.3-alpine"))
                    .withDatabaseName("integration-tests-db")
                    .withUsername("username")
                    .withPassword("password");

    static {
        sqlContainer.start();
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                        "spring.datasource.url=" + sqlContainer.getJdbcUrl(),
                        "spring.datasource.username=" + sqlContainer.getUsername(),
                        "spring.datasource.password=" + sqlContainer.getPassword())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
