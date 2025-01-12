package com.example.demo.listener;

import jakarta.validation.constraints.Pattern;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Validated
class NotificationController {

    private final NotificationListener notificationListener;

    NotificationController(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;
    }

    @GetMapping(path = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<NotificationEvent> streamNotifications() {
        return notificationListener.getNotificationStream();
    }

    @GetMapping(path = "/notifications/{channel}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<NotificationEvent> streamChannelNotifications(
            @PathVariable
                    @Pattern(
                            regexp = "[a-zA-Z0-9_]+",
                            message = "Channel name must contain only letters, numbers, and underscores")
                    String channel) {
        return notificationListener.listenTo(channel).thenMany(notificationListener.getNotificationStream(channel));
    }
}
