package com.example.ultimateredis.bootstrap;

import com.example.ultimateredis.model.Actor;
import com.example.ultimateredis.service.ActorService;
import com.example.ultimateredis.service.CacheServiceWithCustomKey;
import com.example.ultimateredis.service.ControlledCacheServiceWithGenericKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    private final CacheServiceWithCustomKey cacheService;
    private final ActorService actorService;
    private final ControlledCacheServiceWithGenericKey controlledCacheServiceWithGenericKey;

    @Override
    public void run(String... args) {
        String firstString = cacheService.cacheThis("param1", UUID.randomUUID().toString());
        log.info("First: {}", firstString);
        String secondString = cacheService.cacheThis("param1", UUID.randomUUID().toString());
        log.info("Second: {}", secondString);
        String thirdString = cacheService.cacheThis("AnotherParam", UUID.randomUUID().toString());
        log.info("Third: {}", thirdString);
        String fourthString = cacheService.cacheThis("AnotherParam", UUID.randomUUID().toString());
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

        Actor savedActor = actorService.saveActor(new Actor(null, "sampleName", 30));
        log.info("Saved Actor using Data: {}", savedActor);
        Optional<Actor> sampleName = actorService.findActorByName("sampleName");
        log.info("Fetched Actor using Data: {}", sampleName.get());
        actorService.deleteActorById(savedActor.getId());
        log.info("deleted Actor: {}", actorService.findActorById(savedActor.getId()).isEmpty());

        List<Actor> savedActors =
                actorService.saveActors(
                        List.of(new Actor(null, "tom", 30), new Actor(null, "brad", 45)));
        Optional<Actor> actorTom = actorService.findActorByNameAndAge("tom", 30);
        log.info("Fetched Actor with Age using Data: {}", actorTom.get());
        sampleName = actorService.findActorByNameAndAge("tom", 60);
        log.info("Fetched Actor: {}", sampleName.isPresent());
    }

    private String getFromControlledCache(String param) {
        String fromCache = controlledCacheServiceWithGenericKey.getFromCache(param);
        if (fromCache == null) {
            log.info("Oops - Cache was empty. Going to populate it");
            String newValue =
                    controlledCacheServiceWithGenericKey.populateCache(
                            param, UUID.randomUUID().toString());
            log.info("Populated Cache with: {}", newValue);
            return newValue;
        }
        log.info("Returning from Cache: {}", fromCache);
        return fromCache;
    }
}
