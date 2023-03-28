package com.example.jooq.r2dbc.common;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

public class DBContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("integration-tests-db")
                    .withUsername("username")
                    .withPassword("password")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("init.sql"),
                            "/docker-entrypoint-initdb.d/init.sql");
    ;

    static {
        postgreSQLContainer.start();
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                        "spring.flyway.url=" + postgreSQLContainer.getJdbcUrl(),
                        "spring.r2dbc.username=" + postgreSQLContainer.getUsername(),
                        "spring.r2dbc.password=" + postgreSQLContainer.getPassword(),
                        "spring.r2dbc.url= r2dbc:postgresql://"
                                + postgreSQLContainer.getHost()
                                + ":"
                                + postgreSQLContainer.getFirstMappedPort()
                                + "/"
                                + postgreSQLContainer.getDatabaseName())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
