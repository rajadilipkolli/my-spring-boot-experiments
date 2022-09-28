package com.example.hibernatecache.common;

import java.time.Duration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

public class DBContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> sqlContainer =
            new PostgreSQLContainer<>("postgres:14-alpine")
                    .withDatabaseName("integration-tests-db")
                    .withUsername("username")
                    .withPassword("password");

    private static final GenericContainer redisContainer =
            new GenericContainer<>(DockerImageName.parse("redis"))
                    .withExposedPorts(6379)
                    .withStartupTimeout(Duration.ofMinutes(3));

    static {
        Startables.deepStart(sqlContainer, redisContainer).join();
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                        "spring.datasource.url=" + sqlContainer.getJdbcUrl(),
                        "spring.datasource.username=" + sqlContainer.getUsername(),
                        "spring.datasource.password=" + sqlContainer.getPassword(),
                        "spring.redis.host=" + redisContainer.getHost(),
                        "spring.redis.port=" + redisContainer.getMappedPort(6379).toString())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
