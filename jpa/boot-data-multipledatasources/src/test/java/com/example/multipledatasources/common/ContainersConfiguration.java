package com.example.multipledatasources.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration {

    @Bean
    MySQLContainer<?> mySQLContainer() {
        MySQLContainer<?> mysql =
                new MySQLContainer<>(DockerImageName.parse("mysql").withTag("9.1"));
        mysql.start();
        return mysql;
    }

    @Bean
    PostgreSQLContainer<?> postgreSQLContainer() {
        PostgreSQLContainer<?> postgres =
                new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("17.0-alpine"));
        postgres.start();
        return postgres;
    }

    @Bean
    public DynamicPropertyRegistrar kafkaProperties(
            MySQLContainer<?> mySQLContainer, PostgreSQLContainer<?> postgreSQLContainer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            mySQLContainer.stop();
            postgreSQLContainer.stop();
        }));
        return (properties) -> {
            // Connect our Spring application to our Testcontainers instances
            properties.add("app.datasource.cardholder.url", mySQLContainer::getJdbcUrl);
            properties.add("app.datasource.cardholder.username", mySQLContainer::getUsername);
            properties.add("app.datasource.cardholder.password", mySQLContainer::getPassword);
            properties.add("app.datasource.member.url", postgreSQLContainer::getJdbcUrl);
            properties.add("app.datasource.member.username", postgreSQLContainer::getUsername);
            properties.add("app.datasource.member.password", postgreSQLContainer::getPassword);
        };
    }
}
