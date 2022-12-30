package com.example.multitenancy.common;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

public class DBContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("integration-tests-db")
                    .withUsername("username")
                    .withPassword("password");

    private static final MariaDBContainer<?> MARIA_DB_CONTAINER =
            new MariaDBContainer<>("mariadb:10.10")
                    .withDatabaseName("integration-tests-db")
                    .withUsername("username")
                    .withPassword("password");

    static {
        Startables.deepStart(POSTGRESQL_CONTAINER, MARIA_DB_CONTAINER).join();
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                        "datasource.primary.url=" + POSTGRESQL_CONTAINER.getJdbcUrl(),
                        "datasource.primary.username=" + POSTGRESQL_CONTAINER.getUsername(),
                        "datasource.primary.password=" + POSTGRESQL_CONTAINER.getPassword(),
                        "datasource.secondary.url=" + MARIA_DB_CONTAINER.getJdbcUrl(),
                        "datasource.secondary.username=" + MARIA_DB_CONTAINER.getUsername(),
                        "datasource.secondary.password=" + MARIA_DB_CONTAINER.getPassword())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
