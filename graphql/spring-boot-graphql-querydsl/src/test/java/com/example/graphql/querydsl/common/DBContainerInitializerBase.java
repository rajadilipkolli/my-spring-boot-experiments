package com.example.graphql.querydsl.common;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public interface DBContainerInitializerBase {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:15.3-alpine").withReuse(true);
}
