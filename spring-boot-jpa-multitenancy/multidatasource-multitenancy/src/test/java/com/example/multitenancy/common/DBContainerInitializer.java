package com.example.multitenancy.common;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

public class DBContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> sqlContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("integration-tests-db")
                    .withUsername("username")
                    .withPassword("password");

    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("integration-tests")
                    .withUsername("username")
                    .withPassword("password");

    static {
        Startables.deepStart(sqlContainer, POSTGRE_SQL_CONTAINER).join();
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                        "datasource.primary.url=" + sqlContainer.getJdbcUrl(),
                        "datasource.primary.username=" + sqlContainer.getUsername(),
                        "datasource.primary.password=" + sqlContainer.getPassword(),
                        "datasource.secondary.url=" + POSTGRE_SQL_CONTAINER.getJdbcUrl(),
                        "datasource.secondary.username=" + POSTGRE_SQL_CONTAINER.getUsername(),
                        "datasource.secondary.password=" + POSTGRE_SQL_CONTAINER.getPassword())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
