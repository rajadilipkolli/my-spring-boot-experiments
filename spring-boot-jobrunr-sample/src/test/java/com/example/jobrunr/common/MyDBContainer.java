package com.example.jobrunr.common;

import org.testcontainers.containers.PostgreSQLContainer;

public interface MyDBContainer {
    PostgreSQLContainer<?> sqlContainer = new PostgreSQLContainer<>("postgres:15.3-alpine");
}
