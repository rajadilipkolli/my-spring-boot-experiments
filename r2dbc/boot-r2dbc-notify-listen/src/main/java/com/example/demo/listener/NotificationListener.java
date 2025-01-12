package com.example.demo.listener;

import com.example.demo.config.NotificationProperties;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

@Service
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final PostgresqlConnection postgresqlConnection;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationProperties notificationProperties;
    private final Sinks.Many<NotificationEvent> notificationSink;

    public NotificationListener(
            PostgresqlConnection postgresqlConnection,
            ApplicationEventPublisher eventPublisher,
            NotificationProperties notificationProperties) {
        this.postgresqlConnection = postgresqlConnection;
        this.eventPublisher = eventPublisher;
        this.notificationProperties = notificationProperties;
        this.notificationSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @PostConstruct
    public void initialize() {
        // Listen to the configured channel by default
        listenTo(notificationProperties.channelName())
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .subscribe(
                        postgresqlResult -> log.debug(postgresqlResult.toString()),
                        error -> log.error("Fatal error in channel subscription", error));
        // Subscribe to notifications
        postgresqlConnection
                .getNotifications()
                .limitRate(100)
                .delayElements(Duration.ofSeconds(1))
                .map(notification -> new NotificationEvent(
                        notification.getName(), notification.getParameter(), notification.getProcessId()))
                .doOnNext(notificationEvent -> {
                    // Publish Spring notificationEvent
                    eventPublisher.publishEvent(notificationEvent);
                    // Emit to reactive stream
                    Sinks.EmitResult emitResult = notificationSink.tryEmitNext(notificationEvent);
                    if (emitResult.isFailure()) {
                        log.error("Failed to emit notification: {}", emitResult);
                    }
                })
                .doOnError(error -> log.error("Error receiving notification", error))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .subscribe(null, error -> log.error("Fatal error in notification subscription", error));
    }

    public Mono<PostgresqlResult> listenTo(String channel) {
        return Mono.from(postgresqlConnection
                        .createStatement("LISTEN %s".formatted(channel))
                        .execute())
                .doOnSuccess(result -> log.info("Listening on channel: {}", channel))
                .doOnError(error -> log.error("Error listening to channel: {}", channel, error));
    }

    public Flux<NotificationEvent> getNotificationStream() {
        return notificationSink.asFlux();
    }

    public Flux<NotificationEvent> getNotificationStream(String channel) {
        return notificationSink.asFlux().filter(event -> event.channel().equals(channel));
    }

    @PreDestroy
    public void cleanup() {
        // Complete the sink
        notificationSink.tryEmitComplete();

        // Unsubscribe from the channel
        Mono.from(postgresqlConnection
                        .createStatement("UNLISTEN " + notificationProperties.channelName())
                        .execute())
                .then(postgresqlConnection.close())
                .doOnError(error -> log.error("Error closing connection", error))
                .doOnSuccess(unused -> log.info("Notification listener connection closed"))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .block(Duration.ofSeconds(5));
    }
}
