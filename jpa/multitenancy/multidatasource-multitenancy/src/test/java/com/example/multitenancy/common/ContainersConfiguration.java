package com.example.multitenancy.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration {

    @Bean
    PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres").withTag("18.2-alpine"));
    }

    @Bean
    OracleContainer oracleContainer() {
        return new OracleContainer(DockerImageName.parse("gvenzl/oracle-free").withTag("23-slim-faststart"))
                .withReuse(true);
    }

    @Bean
    DynamicPropertyRegistrar databaseProperties(
            OracleContainer oracleContainer, PostgreSQLContainer postgreSQLContainer) {
        return (propertyRegistry) -> {
            // Connect our Spring application to our Testcontainers instances
            propertyRegistry.add("datasource.primary.url", oracleContainer::getJdbcUrl);
            propertyRegistry.add("datasource.primary.username", oracleContainer::getUsername);
            propertyRegistry.add("datasource.primary.password", oracleContainer::getPassword);
            propertyRegistry.add("datasource.secondary.url", postgreSQLContainer::getJdbcUrl);
            propertyRegistry.add("datasource.secondary.username", postgreSQLContainer::getUsername);
            propertyRegistry.add("datasource.secondary.password", postgreSQLContainer::getPassword);
        };
    }
}
