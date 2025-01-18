package com.example.mongoes.config;

import com.example.mongoes.web.service.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ChangeStreamStartupListener {

    private static final Logger log = LoggerFactory.getLogger(ChangeStreamStartupListener.class);

    private final RestaurantService restaurantService;

    public ChangeStreamStartupListener(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void startListeningToChangeStream() {
        log.info("Initializing MongoDB change stream listener");
        restaurantService
                .changeStreamProcessor()
                .log()
                .doOnError(error -> log.error("Error in change stream: {}", error.getMessage()))
                .doOnComplete(() -> log.info("Change stream completed"))
                .subscribe();
    }
}
