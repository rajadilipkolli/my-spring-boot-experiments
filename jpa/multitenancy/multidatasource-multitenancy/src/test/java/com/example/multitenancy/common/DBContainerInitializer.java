package com.example.multitenancy.common;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

public class DBContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> PRIMARY_POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:15.3-alpine");

    private static final OracleContainer ORACLE_CONTAINER =
            new OracleContainer("gvenzl/oracle-xe:21-slim");

    static {
        Startables.deepStart(PRIMARY_POSTGRE_SQL_CONTAINER, ORACLE_CONTAINER).join();
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                        "datasource.primary.url=" + ORACLE_CONTAINER.getJdbcUrl(),
                        "datasource.primary.username=" + ORACLE_CONTAINER.getUsername(),
                        "datasource.primary.password=" + ORACLE_CONTAINER.getPassword(),
                        "datasource.secondary.url=" + PRIMARY_POSTGRE_SQL_CONTAINER.getJdbcUrl(),
                        "datasource.secondary.username="
                                + PRIMARY_POSTGRE_SQL_CONTAINER.getUsername(),
                        "datasource.secondary.password="
                                + PRIMARY_POSTGRE_SQL_CONTAINER.getPassword())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
