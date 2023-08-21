package com.example.multitenancy.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true, parallel = true)
public class DBContainerInitializer {

    @Container
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:15.4-alpine");

    @Container
    private static final OracleContainer ORACLE_CONTAINER =
            new OracleContainer("gvenzl/oracle-xe:21.3.0-slim");

    @DynamicPropertySource
    static void addsDynamicProperties(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("datasource.primary.url", ORACLE_CONTAINER::getJdbcUrl);
        propertyRegistry.add("datasource.primary.username", ORACLE_CONTAINER::getUsername);
        propertyRegistry.add("datasource.primary.password", ORACLE_CONTAINER::getPassword);
        propertyRegistry.add("datasource.secondary.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        propertyRegistry.add("datasource.secondary.username", POSTGRE_SQL_CONTAINER::getUsername);
        propertyRegistry.add("datasource.secondary.password", POSTGRE_SQL_CONTAINER::getPassword);
    }
}
