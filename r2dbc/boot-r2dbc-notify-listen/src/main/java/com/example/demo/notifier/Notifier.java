package com.example.demo.notifier;

import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import jakarta.annotation.PreDestroy;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Notifier {

    private static final Logger log = LoggerFactory.getLogger(Notifier.class);

    private final PostgresqlConnection sender;

    public Notifier(PostgresqlConnection receiver) {
        this.sender = receiver;
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
}
