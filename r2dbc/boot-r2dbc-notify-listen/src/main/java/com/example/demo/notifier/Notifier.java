package com.example.demo.notifier;

import java.util.Map;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class Notifier {

    private final DatabaseClient databaseClient;

    public Notifier(DatabaseClient databaseClient) {

        this.databaseClient = databaseClient;
    }

    public Mono<ServerResponse> notifyData(ServerRequest req) {
        return req.bodyToMono(NotificationRequest.class).flatMap(notificationRequest -> databaseClient
                .sql("SELECT pg_notify(:channel, :message)")
                .bind("channel", notificationRequest.channel())
                .bind("message", notificationRequest.message())
                .fetch()
                .first()
                .then(ServerResponse.ok()
                        .bodyValue(Map.of(
                                "status", "success",
                                "message", "Notification sent",
                                "channel", notificationRequest.channel()))));
    }
}
