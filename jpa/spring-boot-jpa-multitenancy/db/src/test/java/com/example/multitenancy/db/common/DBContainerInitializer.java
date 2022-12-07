package com.example.multitenancy.db.common;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

public class DBContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> sqlContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("integration-tests-db")
                    .withUsername("username")
                    .withPassword("password");

    private static final MariaDBContainer<?> mariaDBContainer =
            new MariaDBContainer<>("mariadb")
                    .withDatabaseName("integration-tests-db")
                    .withUsername("username")
                    .withPassword("password");

    static {
        Startables.deepStart(sqlContainer, mariaDBContainer).join();
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                        "datasource.primary.url=" + sqlContainer.getJdbcUrl(),
                        "datasource.primary.username=" + sqlContainer.getUsername(),
                        "datasource.primary.password=" + sqlContainer.getPassword(),
                        "datasource.secondary.url=" + mariaDBContainer.getJdbcUrl(),
                        "datasource.secondary.username=" + mariaDBContainer.getUsername(),
                        "datasource.secondary.password=" + mariaDBContainer.getPassword())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
