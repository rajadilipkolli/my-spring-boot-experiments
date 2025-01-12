package com.example.demo.listener;

import io.r2dbc.postgresql.api.PostgresqlConnection;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final PostgresqlConnection connection;
    private final ApplicationEventPublisher eventPublisher;
    private final Sinks.Many<NotificationEvent> notificationSink =
            Sinks.many().multicast().onBackpressureBuffer();

    public NotificationListener(PostgresqlConnection connection, ApplicationEventPublisher eventPublisher) {
        this.connection = connection;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void initialize() {
        // Subscribe to notifications
        connection
                .getNotifications()
                .doOnNext(notification -> {
                    var event = new NotificationEvent(
                            notification.getName(), notification.getParameter(), notification.getProcessId());
                    // Publish Spring event
                    eventPublisher.publishEvent(event);
                    // Emit to reactive stream
                    notificationSink.tryEmitNext(event);
                })
                .doOnError(error -> log.error("Error receiving notification", error))
                .subscribe();
    }

    public Mono<Void> listenTo(String channel) {
        return Mono.from(connection.createStatement("LISTEN " + channel).execute())
                .doOnSuccess(result -> log.info("Listening on channel: {}", channel))
                .doOnError(error -> log.error("Error listening to channel: {}", channel, error))
                .then();
    }

    public Flux<NotificationEvent> getNotificationStream() {
        return notificationSink.asFlux();
    }

    public Flux<NotificationEvent> getNotificationStream(String channel) {
        return notificationSink.asFlux().filter(event -> event.channel().equals(channel));
    }

    @PreDestroy
    public void cleanup() {
        connection
                .close()
                .subscribe(
                        null,
                        error -> log.error("Error closing connection", error),
                        () -> log.info("Notification listener connection closed"));
    }
}
