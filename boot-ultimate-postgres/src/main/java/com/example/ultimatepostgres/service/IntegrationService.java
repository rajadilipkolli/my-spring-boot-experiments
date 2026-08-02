package com.example.ultimatepostgres.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

@Service
public class IntegrationService {

    private final CacheService cacheService;
    private final JobProducerService jobProducerService;
    private final PubSubPublisher pubSubPublisher;

    public IntegrationService(
            CacheService cacheService, JobProducerService jobProducerService, PubSubPublisher pubSubPublisher) {
        this.cacheService = cacheService;
        this.jobProducerService = jobProducerService;
        this.pubSubPublisher = pubSubPublisher;
    }

    @Transactional
    public void executeCombinedOperation(String id, JsonNode payload) {
        cacheService.put("combined:" + id, payload, 60000);
        jobProducerService.enqueue(payload, 10);
        pubSubPublisher.publish("Combined operation executed for id: " + id);
    }
}
