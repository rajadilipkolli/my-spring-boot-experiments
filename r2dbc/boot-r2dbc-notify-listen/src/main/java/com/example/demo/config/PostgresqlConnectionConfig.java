package com.example.demo.config;

import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Wrapped;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
public class PostgresqlConnectionConfig {

    private final ConnectionFactory connectionFactory;

    public PostgresqlConnectionConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    PostgresqlConnection postgresqlConnection() {
        return Mono.from(connectionFactory.create())
                .flatMap(this::unwrapToPostgresqlConnection)
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Failed to create PostgresqlConnection"));
    }

    private Mono<PostgresqlConnection> unwrapToPostgresqlConnection(Connection connection) {
        if (connection instanceof Wrapped) {
            PostgresqlConnection delegate = ((Wrapped<Connection>) connection).unwrap(PostgresqlConnection.class);
            if (delegate != null) {
                return Mono.just(delegate);
            }
        } else if (connection instanceof PostgresqlConnection postgresqlConnection) {
            return Mono.just(postgresqlConnection);
        }
        return Mono.error(new IllegalStateException("Connection is not a PostgresqlConnection"));
    }
}
