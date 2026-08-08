package com.example.ultimateredis.config;

import com.example.ultimateredis.service.ActorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CacheWarmingRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmingRunner.class);
    private final ActorService actorService;

    public CacheWarmingRunner(ActorService actorService) {
        this.actorService = actorService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting cache warming...");
        try {
            // Warm the actor cache by finding top actors
            actorService.findActorByName("Alice");
            actorService.findActorByName("Bob");
            log.info("Cache warming completed successfully.");
        } catch (Exception e) {
            log.warn("Cache warming failed: {}", e.getMessage());
        }
    }
}
