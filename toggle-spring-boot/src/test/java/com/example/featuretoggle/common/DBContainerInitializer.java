package com.example.featuretoggle.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;

@Slf4j
public class DBContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final MySQLContainer<?> sqlContainer =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("integration-tests-db")
                    .withUsername("username")
                    .withPassword("password");

    static {
        sqlContainer.start();
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                        "spring.datasource.url=" + sqlContainer.getJdbcUrl(),
                        "spring.datasource.username=" + sqlContainer.getUsername(),
                        "spring.datasource.password=" + sqlContainer.getPassword())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
