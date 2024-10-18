package com.example.multipledatasources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestMultipleDataSourcesApplication {

    private static final MySQLContainer<?> MY_SQL_CONTAINER =
            new MySQLContainer<>(DockerImageName.parse("mysql").withTag("9.1"));

    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("17.0-alpine"));

    static {
        Startables.deepStart(MY_SQL_CONTAINER, POSTGRE_SQL_CONTAINER).join();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MY_SQL_CONTAINER.stop();
            POSTGRE_SQL_CONTAINER.stop();
        }));
    }

    public static void main(String[] args) {
        System.setProperty("app.datasource.cardholder.url", MY_SQL_CONTAINER.getJdbcUrl());
        System.setProperty("app.datasource.cardholder.username", MY_SQL_CONTAINER.getUsername());
        System.setProperty("app.datasource.cardholder.password", MY_SQL_CONTAINER.getPassword());
        System.setProperty("app.datasource.member.url", POSTGRE_SQL_CONTAINER.getJdbcUrl());
        System.setProperty("app.datasource.member.username", POSTGRE_SQL_CONTAINER.getUsername());
        System.setProperty("app.datasource.member.password", POSTGRE_SQL_CONTAINER.getPassword());
        SpringApplication.from(MultipleDataSourcesApplication::main)
                .with(TestMultipleDataSourcesApplication.class)
                .run(args);
    }
}
