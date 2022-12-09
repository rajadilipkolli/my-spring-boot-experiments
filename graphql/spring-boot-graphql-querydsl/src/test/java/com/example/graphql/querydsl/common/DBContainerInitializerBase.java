package com.example.graphql.querydsl.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public class DBContainerInitializerBase {

    protected static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("integration-tests-db")
                    .withUsername("username")
                    .withPassword("password")
                    .withReuse(true);

    static {
        POSTGRE_SQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void setApplicationProperties(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        propertyRegistry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
        propertyRegistry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
    }
}
