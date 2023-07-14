package com.example.ultimateredis.bootstrap;

import com.example.ultimateredis.service.CacheServiceWithCustomKey;
import com.example.ultimateredis.service.ControlledCacheServiceWithGenericKey;
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
