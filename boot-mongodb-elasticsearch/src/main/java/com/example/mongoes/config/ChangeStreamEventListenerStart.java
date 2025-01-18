package com.example.mongoes.config;

import com.example.mongoes.web.service.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ChangeStreamEventListenerStart {

    private static final Logger log = LoggerFactory.getLogger(ChangeStreamEventListenerStart.class);

    private final RestaurantService restaurantService;

    public ChangeStreamEventListenerStart(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void startListeningToChangeStream() {
        log.info("Inside ApplicationStartedEvent");
        restaurantService.changeStreamProcessor().log().subscribe();
    }
}
