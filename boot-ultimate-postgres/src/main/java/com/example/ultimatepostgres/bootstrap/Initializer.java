package com.example.ultimatepostgres.bootstrap;

import com.example.ultimatepostgres.service.CacheService;
import com.example.ultimatepostgres.service.JobProducerService;
import com.example.ultimatepostgres.service.PubSubPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    private final CacheService cacheService;
    private final JobProducerService jobProducerService;
    private final PubSubPublisher pubSubPublisher;
    private final ObjectMapper objectMapper;

    public Initializer(
            CacheService cacheService,
            JobProducerService jobProducerService,
            PubSubPublisher pubSubPublisher,
            ObjectMapper objectMapper) {
        this.cacheService = cacheService;
        this.jobProducerService = jobProducerService;
        this.pubSubPublisher = pubSubPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        log.info("Running Initializer to demonstrate Cache, Queue and PubSub...");

        cacheService.put("startup:greeting", objectMapper.readTree("""
                {"message": "Hello from Cache!"}"""), 60000);
        log.info("Added item to cache");

        jobProducerService.enqueue(objectMapper.readTree("""
                {"task":"startup-job"}"""), 10);
        log.info("Enqueued startup job");

        pubSubPublisher.publish("""
                {"message": "Startup completed"}""");
        log.info("Published startup message");
    }
}
