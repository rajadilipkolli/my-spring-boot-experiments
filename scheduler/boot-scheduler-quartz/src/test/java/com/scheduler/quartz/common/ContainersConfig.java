package com.scheduler.quartz.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("18.0-alpine"));
    }

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(PostgreSQLContainer<?> postgreSQLContainer) {
        return (registrar) -> {
            registrar.add(
                    "spring.quartz.properties.org.quartz.dataSource.quartzDS.URL", postgreSQLContainer::getJdbcUrl);
            registrar.add(
                    "spring.quartz.properties.org.quartz.dataSource.quartzDS.user", postgreSQLContainer::getUsername);
            registrar.add(
                    "spring.quartz.properties.org.quartz.dataSource.quartzDS.password",
                    postgreSQLContainer::getPassword);
        };
    }
}
