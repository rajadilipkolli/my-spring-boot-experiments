package com.example.ultimateredis.bootstrap;

import com.example.ultimateredis.model.Actor;
import com.example.ultimateredis.service.ActorService;
import com.example.ultimateredis.service.CacheServiceWithCustomKey;
import com.example.ultimateredis.service.ControlledCacheServiceWithGenericKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    private final CacheServiceWithCustomKey cacheService;
    private final ActorService actorService;
    private final ControlledCacheServiceWithGenericKey controlledCacheServiceWithGenericKey;

    public Initializer(
            CacheServiceWithCustomKey cacheService,
            ActorService actorService,
            ControlledCacheServiceWithGenericKey controlledCacheServiceWithGenericKey) {
        this.cacheService = cacheService;
        this.actorService = actorService;
        this.controlledCacheServiceWithGenericKey = controlledCacheServiceWithGenericKey;
    }

    @Override
    public void run(String... args) {
        String firstString = cacheService.cacheThis("param1", UUID.randomUUID().toString());
        log.info("First: {}", firstString);
        String secondString = cacheService.cacheThis("param1", UUID.randomUUID().toString());
        log.info("Second: {}", secondString);
        String thirdString =
                cacheService.cacheThis("AnotherParam", UUID.randomUUID().toString());
        log.info("Third: {}", thirdString);
        String fourthString =
                cacheService.cacheThis("AnotherParam", UUID.randomUUID().toString());
        log.info("Fourth: {}", fourthString);

        log.info("Starting controlled cache: -----------");
        String controlledFirst = getFromControlledCache("first");
        log.info("Controlled First: {}", controlledFirst);
        String controlledSecond = getFromControlledCache("second");
        log.info("Controlled Second: {}", controlledSecond);

        getFromControlledCache("first");
        getFromControlledCache("second");
        getFromControlledCache("third");
        log.info("Clearing all cache entries:");
        cacheService.forgetAboutThis("param1");
        controlledCacheServiceWithGenericKey.removeFromCache("controlledParam1");

        Actor savedActor =
                actorService.saveActor(new Actor().setName("sampleName").setAge(30));
        log.info("Saved Actor using Data: {}", savedActor);
        Optional<Actor> sampleName = actorService.findActorByName("sampleName");
        log.info("Fetched Actor using Data: {}", sampleName.get());
        actorService.deleteActorById(savedActor.getId());
        log.info(
                "deleted Actor: {}",
                actorService.findActorById(savedActor.getId()).isEmpty());

        actorService.saveActors(List.of(
                new Actor().setName("tom").setAge(30),
                new Actor().setName("brad").setAge(45)));
        Optional<Actor> actorTom = actorService.findActorByNameAndAge("tom", 30);
        if (actorTom.isPresent()) {
            log.info("Saved Actor using Data: {}", actorTom.get());
            sampleName = actorService.findActorByNameAndAge("tom", 60);
            log.info("Fetched Actor: {}", sampleName.isPresent());
            actorService.deleteActorByName(actorTom.get().getName());
            log.info(
                    "deleted Actor: {}",
                    actorService.findActorById(actorTom.get().getId()).isEmpty());
        }
        actorService.deleteAll();
        log.info("No of Entries Present in Cache : {}", actorService.findAll().size());
    }

    private String getFromControlledCache(String param) {
        String fromCache = controlledCacheServiceWithGenericKey.getFromCache(param);
        if (fromCache == null) {
            log.info("Oops - Cache was empty. Going to populate it");
            String newValue = controlledCacheServiceWithGenericKey.populateCache(
                    param, UUID.randomUUID().toString());
            log.info("Populated Cache with: {}", newValue);
            return newValue;
        }
        log.info("Returning from Cache: {}", fromCache);
        return fromCache;
    }
}
