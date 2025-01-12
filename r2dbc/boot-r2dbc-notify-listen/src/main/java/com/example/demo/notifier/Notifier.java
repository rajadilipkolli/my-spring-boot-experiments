package com.example.demo.notifier;

import com.example.demo.listener.NotificationListener;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
                .flatMap(notificationRequest -> {
                    Validator validator =
                            Validation.buildDefaultValidatorFactory().getValidator();
                    Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(notificationRequest);
                    if (!violations.isEmpty()) {
                        String errorMessage = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));
                        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                                .bodyValue(problem);
                    }
                    return notificationListener
                            .listenTo(notificationRequest.channel())
                            .doOnSuccess(postgresqlResult -> log.debug(postgresqlResult.toString()))
                            .then(Mono.defer(() -> sendNotification(notificationRequest)
                                    .flatMap(rowsUpdated -> ServerResponse.ok().bodyValue(rowsUpdated))
                                    .onErrorResume(e -> {
                                        log.error("Error sending notification: {}", e.getMessage(), e);
                                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .bodyValue("Error sending notification: " + e.getMessage());
                                    })));
                })
                .onErrorResume(error -> {
                    log.error("Failed to process notification request", error);
                    return ServerResponse.status(500).bodyValue("Failed to process notification request");
                });
    }

    private Mono<Long> sendNotification(NotificationRequest notificationRequest) {
        log.debug("Channel {} registered, sending notification", notificationRequest.channel());
        return databaseClient
                .sql("SELECT pg_notify(:channel, :message)")
                .bind("channel", notificationRequest.channel())
                .bind("message", notificationRequest.message())
                .fetch()
                .rowsUpdated();
    }
}
