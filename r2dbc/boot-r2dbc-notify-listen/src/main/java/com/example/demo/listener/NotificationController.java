package com.example.demo.listener;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class NotificationController {

    private final NotificationListener notificationListener;

    public NotificationController(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;
    }

    @GetMapping(path = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificationEvent> streamNotifications() {
        return notificationListener.getNotificationStream();
    }

    @GetMapping(path = "/notifications/{channel}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificationEvent> streamChannelNotifications(@PathVariable String channel) {
        return notificationListener.listenTo(channel).thenMany(notificationListener.getNotificationStream(channel));
    }
}
