package com.example.multipledatasources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestMultipleDataSourcesApplication {

    private static final MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer<>("mysql:8.4");

    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.3-alpine"));

    static {
        Startables.deepStart(MY_SQL_CONTAINER, POSTGRE_SQL_CONTAINER).join();
        System.setProperty("app.datasource.cardholder.url", MY_SQL_CONTAINER.getJdbcUrl());
        System.setProperty("app.datasource.cardholder.username", MY_SQL_CONTAINER.getUsername());
        System.setProperty("app.datasource.cardholder.password", MY_SQL_CONTAINER.getPassword());
        System.setProperty("app.datasource.member.url", POSTGRE_SQL_CONTAINER.getJdbcUrl());
        System.setProperty("app.datasource.member.username", POSTGRE_SQL_CONTAINER.getUsername());
        System.setProperty("app.datasource.member.password", POSTGRE_SQL_CONTAINER.getPassword());
    }

    public static void main(String[] args) {
        SpringApplication.from(MultipleDataSourcesApplication::main)
                .with(TestMultipleDataSourcesApplication.class)
                .run(args);
    }
}
