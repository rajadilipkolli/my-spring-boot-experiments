package com.example.multitenancy.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true, parallel = true)
public class DBContainerInitializer {

    @Container
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("16.1-alpine"));

    @Container
    private static final OracleContainer ORACLE_CONTAINER =
            new OracleContainer(
                            DockerImageName.parse("gvenzl/oracle-free")
                                    .withTag("23-slim-faststart"))
                    .withReuse(true);

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
