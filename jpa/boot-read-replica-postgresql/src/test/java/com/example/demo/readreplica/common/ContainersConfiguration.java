package com.example.demo.readreplica.common;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration {

    @Bean
    PostgreSQLContainer<?> masterPostgreSQLContainer() {
        Map<String, String> envMap = new HashMap<>();
        envMap.put("POSTGRESQL_REPLICATION_MODE", "master");
        envMap.put("POSTGRESQL_REPLICATION_USER", "repl_user");
        envMap.put("POSTGRESQL_REPLICATION_PASSWORD", "repl_password");
        envMap.put("POSTGRESQL_USERNAME", "postgres_write");
        envMap.put("POSTGRESQL_PASSWORD", "postgres_write");
        envMap.put("POSTGRESQL_DATABASE", "my_database");

        PostgreSQLContainer<?> master =
                new PostgreSQLContainer<>(
                                DockerImageName.parse("bitnami/postgresql")
                                        .withTag("latest")
                                        .asCompatibleSubstituteFor("postgres"))
                        .withEnv(envMap)
                        .withStartupAttempts(3)
                        .withStartupTimeout(Duration.ofMinutes(3));
        master.start();
        return master;
    }

    @Bean
    PostgreSQLContainer<?> slavePostgreSQLContainer(
            PostgreSQLContainer<?> masterPostgreSQLContainer) {
        Map<String, String> envMap = new HashMap<>();
        envMap.put("POSTGRESQL_REPLICATION_MODE", "slave");
        envMap.put("POSTGRESQL_REPLICATION_USER", "repl_user");
        envMap.put("POSTGRESQL_REPLICATION_PASSWORD", "repl_password");
        envMap.put("POSTGRESQL_USERNAME", "postgres_write");
        envMap.put("POSTGRESQL_PASSWORD", "postgres_write");
        envMap.put("POSTGRESQL_MASTER_HOST", masterPostgreSQLContainer.getHost());
        envMap.put(
                "POSTGRESQL_MASTER_PORT_NUMBER",
                String.valueOf(masterPostgreSQLContainer.getMappedPort(5432)));

        PostgreSQLContainer<?> slave =
                new PostgreSQLContainer<>(
                                DockerImageName.parse("bitnami/postgresql:latest")
                                        .asCompatibleSubstituteFor("postgres"))
                        .withEnv(envMap)
                        .withStartupAttempts(3)
                        .withStartupTimeout(Duration.ofMinutes(3));
        slave.start();
        return slave;
    }

    @Bean
    public DynamicPropertyRegistrar databaseProperties(
            PostgreSQLContainer<?> masterPostgreSQLContainer,
            PostgreSQLContainer<?> slavePostgreSQLContainer) {
        return (properties) -> {
            properties.add("spring.primary.datasource.url", masterPostgreSQLContainer::getJdbcUrl);
            properties.add("spring.replica.datasource.url", slavePostgreSQLContainer::getJdbcUrl);
        };
    }
}
