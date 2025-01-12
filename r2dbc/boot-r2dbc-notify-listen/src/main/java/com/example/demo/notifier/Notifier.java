package com.example.demo.notifier;

import com.example.demo.listener.NotificationListener;
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

    private final DatabaseClient databaseClient;
    private final NotificationListener notificationListener;

    public Notifier(DatabaseClient databaseClient, NotificationListener notificationListener) {
        this.databaseClient = databaseClient;
        this.notificationListener = notificationListener;
    }

    public Mono<ServerResponse> notifyData(ServerRequest req) {
        return req.bodyToMono(NotificationRequest.class)
                .flatMap(notificationRequest -> notificationListener
                        .listenTo(notificationRequest.channel())
                        .doOnSuccess(postgresqlResult -> log.debug(postgresqlResult.toString()))
                        .then(Mono.defer(() -> sendNotification(notificationRequest))))
                .onErrorResume(error -> {
                    log.error("Failed to process notification request", error);
                    return ServerResponse.status(500).bodyValue("Failed to process notification request");
                });
    }

    private Mono<ServerResponse> sendNotification(NotificationRequest notificationRequest) {
        log.debug("Channel {} registered, sending notification", notificationRequest.channel());
        return databaseClient
                .sql("SELECT pg_notify(:channel, :message)")
                .bind("channel", notificationRequest.channel())
                .bind("message", notificationRequest.message())
                .fetch()
                .rowsUpdated()
                .flatMap(rowsUpdated -> ServerResponse.ok().bodyValue(rowsUpdated))
                .onErrorResume(error -> ServerResponse.status(500).bodyValue("Failed to send notification"));
    }
}
