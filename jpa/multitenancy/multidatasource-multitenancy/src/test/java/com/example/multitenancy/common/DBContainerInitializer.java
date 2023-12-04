package com.example.multitenancy.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

public class DBContainerInitializer {

    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("16.1-alpine"))
                    .withReuse(true);

    private static final OracleContainer ORACLE_CONTAINER =
            new OracleContainer(
                            DockerImageName.parse("gvenzl/oracle-free")
                                    .withTag("23-slim-faststart"))
                    .withReuse(true);

    static {
        Startables.deepStart(POSTGRE_SQL_CONTAINER, ORACLE_CONTAINER).join();
    }

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
