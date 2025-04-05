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

    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("17.4-alpine"));

    private static final MariaDBContainer<?> MARIA_DB_CONTAINER =
            new MariaDBContainer<>(DockerImageName.parse("mariadb").withTag("11.6"));

    static {
        Startables.deepStart(POSTGRE_SQL_CONTAINER, MARIA_DB_CONTAINER).join();
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                        "datasource.primary.url=" + POSTGRE_SQL_CONTAINER.getJdbcUrl(),
                        "datasource.primary.username=" + POSTGRE_SQL_CONTAINER.getUsername(),
                        "datasource.primary.password=" + POSTGRE_SQL_CONTAINER.getPassword(),
                        "datasource.secondary.url=" + MARIA_DB_CONTAINER.getJdbcUrl(),
                        "datasource.secondary.username=" + MARIA_DB_CONTAINER.getUsername(),
                        "datasource.secondary.password=" + MARIA_DB_CONTAINER.getPassword())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
