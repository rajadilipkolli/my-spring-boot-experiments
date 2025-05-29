package com.example.multitenancy.db.common;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

public class DBContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("17.5-alpine"));

    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER_1 =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("17.5-alpine"));

    static {
        Startables.deepStart(POSTGRE_SQL_CONTAINER, POSTGRE_SQL_CONTAINER_1).join();
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                        "datasource.primary.url=" + POSTGRE_SQL_CONTAINER.getJdbcUrl(),
                        "datasource.primary.username=" + POSTGRE_SQL_CONTAINER.getUsername(),
                        "datasource.primary.password=" + POSTGRE_SQL_CONTAINER.getPassword(),
                        "datasource.secondary.url=" + POSTGRE_SQL_CONTAINER_1.getJdbcUrl(),
                        "datasource.secondary.username=" + POSTGRE_SQL_CONTAINER_1.getUsername(),
                        "datasource.secondary.password=" + POSTGRE_SQL_CONTAINER_1.getPassword())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
