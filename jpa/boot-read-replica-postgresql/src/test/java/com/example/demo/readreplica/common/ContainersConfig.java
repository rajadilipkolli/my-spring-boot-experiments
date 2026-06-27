package com.example.demo.readreplica.common;

import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @Bean
    public Network network() {
        return Network.newNetwork();
    }

    @Bean
    public PostgreSQLContainer postgresqlMaster(Network network) {
        return new PostgreSQLContainer(DockerImageName.parse("postgres").withTag("18"))
                .withNetwork(network)
                .withNetworkAliases("postgresql-master")
                .withExposedPorts(5432)
                .withUsername("postgres_write")
                .withPassword("postgres_write")
                .withDatabaseName("my_database")
                .withCopyFileToContainer(
                        MountableFile.forHostPath("docker/init-master.sql"),
                        "/docker-entrypoint-initdb.d/01-init-master.sql")
                .withCopyFileToContainer(
                        MountableFile.forHostPath("docker/configure-master.sh"),
                        "/docker-entrypoint-initdb.d/02-configure-master.sh")
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("MASTER_CONTAINER")));
    }

    @Bean
    public GenericContainer<?> postgresqlSlave(
            Network network, PostgreSQLContainer postgresqlMaster) {
        return new GenericContainer<>(DockerImageName.parse("postgres").withTag("18"))
                .withNetwork(network)
                .dependsOn(postgresqlMaster)
                .withExposedPorts(5432)
                .withEnv("POSTGRES_USER", "postgres_write")
                .withEnv("POSTGRES_PASSWORD", "postgres_write")
                .withEnv("PGUSER", "postgres_write")
                .withEnv("POSTGRES_MASTER_HOST", "postgresql-master")
                .withEnv("POSTGRES_MASTER_PORT", "5432")
                .withEnv("REPLICATION_USER", "repl_user")
                .withEnv("REPLICATION_PASSWORD", "repl_password")
                .withCopyFileToContainer(
                        MountableFile.forHostPath("docker/setup-slave.sh"), "/setup-slave.sh")
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("/setup-slave.sh"))
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("SLAVE_CONTAINER")))
                .waitingFor(
                        Wait.forLogMessage(
                                ".*database system is ready to accept read-only connections.*\\s",
                                1));
    }

    @Bean
    public DynamicPropertyRegistrar dynamicPropertyRegistrar(
            GenericContainer<?> postgresqlMaster, GenericContainer<?> postgresqlSlave) {
        return registry -> {
            registry.add(
                    "spring.primary.datasource.url",
                    () ->
                            String.format(
                                    "jdbc:postgresql://%s:%d/my_database?options=-c%%20timezone=UTC",
                                    postgresqlMaster.getHost(),
                                    postgresqlMaster.getFirstMappedPort()));
            registry.add("spring.primary.datasource.username", () -> "postgres_write");
            registry.add("spring.primary.datasource.password", () -> "postgres_write");

            registry.add(
                    "spring.replica.datasource.url",
                    () ->
                            String.format(
                                    "jdbc:postgresql://%s:%d/my_database?options=-c%%20timezone=UTC",
                                    postgresqlSlave.getHost(),
                                    postgresqlSlave.getFirstMappedPort()));

            registry.add("spring.replica.datasource.username", () -> "app_readonly");
            registry.add("spring.replica.datasource.password", () -> "app_readonly_password");
        };
    }
}
