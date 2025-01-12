package com.example.demo.listener;

import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import jakarta.annotation.PreDestroy;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class Notifier {

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
