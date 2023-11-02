package com.example.bootbatchjpa.common;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

public interface TestContainersConfig {

    @ServiceConnection
    PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16.0-alpine");
}
