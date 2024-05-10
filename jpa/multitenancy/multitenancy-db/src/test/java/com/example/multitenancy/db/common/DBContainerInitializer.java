package com.example.multitenancy.db.common;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

public class DBContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> sqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.3-alpine"));

    private static final MariaDBContainer<?> mariaDBContainer = new MariaDBContainer<>("mariadb");

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
