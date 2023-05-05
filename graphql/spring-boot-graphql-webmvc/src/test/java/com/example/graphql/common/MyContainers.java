package com.example.graphql.common;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public interface MyContainers {

    @Container
    PostgreSQLContainer<?> sqlContainer =
            new PostgreSQLContainer<>("postgres:15.2-alpine")
                    .withDatabaseName("integration-tests-db")
                    .withUsername("username")
                    .withPassword("password");
}
