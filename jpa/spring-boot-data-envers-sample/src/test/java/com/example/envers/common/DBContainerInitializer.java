package com.example.envers.common;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public interface DBContainerInitializer {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> sqlContainer = new PostgreSQLContainer<>("postgres:15.3-alpine");
}
