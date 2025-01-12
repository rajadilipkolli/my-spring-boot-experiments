package com.example.demo.notifier;

import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import jakarta.annotation.PreDestroy;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class Notifier {

    private static final Logger log = LoggerFactory.getLogger(Notifier.class);

    private final PostgresqlConnection sender;
    private final DatabaseClient databaseClient;

    public Notifier(PostgresqlConnection receiver, DatabaseClient databaseClient) {
        this.sender = receiver;
        this.databaseClient = databaseClient;
    }

    public Mono<Void> send() {
        return sender.createStatement(
                        MessageFormat.format("NOTIFY mymessage, ''Hello world at {0}''", LocalDateTime.now()))
                .execute()
                .flatMap(PostgresqlResult::getRowsUpdated)
                .doOnNext(rows -> log.info("Notification sent, rows updated: {}", rows))
                .then();
    }

    @PreDestroy
    public void destroy() {
        sender.close().doOnSuccess(v -> log.info("Notifier connection closed")).subscribe();
    }

    public Mono<ServerResponse> notifyData(ServerRequest req) {
        return req.bodyToMono(NotificationRequest.class)
                .flatMap(notificationRequest -> databaseClient
                        .sql("SELECT pg_notify(:channel, :message)")
                        .bind("channel", notificationRequest.channel())
                        .bind("message", notificationRequest.message())
                        .fetch()
                        .first())
                .flatMap(stringObjectMap -> ServerResponse.ok().bodyValue(stringObjectMap));
    }
}
