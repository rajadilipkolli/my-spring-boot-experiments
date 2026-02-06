package com.example.highrps.service;

import com.example.highrps.model.request.EventEnvelope;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;
    private final JsonMapper mapper;
    private final MeterRegistry meterRegistry;
    private final Counter eventsPublishedCounter;
    private final Counter tombstonesPublishedCounter;

    @Value("${app.kafka.events-topic:events}")
    private String eventsTopic;

    private static final Map<String, String> ENTITY_TOPICS = Map.of(
            "post", "posts-aggregates",
            "author", "authors-aggregates");

    public KafkaProducerService(
            KafkaTemplate<String, EventEnvelope> kafkaTemplate, JsonMapper mapper, MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
        this.meterRegistry = meterRegistry;
        // base counters (global)
        this.eventsPublishedCounter = meterRegistry.counter("app.kafka.events.published");
        this.tombstonesPublishedCounter = meterRegistry.counter("app.kafka.tombstones.published");
    }

    /**
     * Publish an EventEnvelope to the default events topic. This wraps the payload as JSON
     * and adds an entity label so Streams branching can route to the right per-entity topic.
     *
     * @return a future that completes when the send succeeds/fails; envelope creation errors complete the future exceptionally
     */
    public CompletableFuture<SendResult<String, EventEnvelope>> publishEnvelope(
            String entity, String key, Object payload) {
        try {
            JsonNode payloadNode = mapper.valueToTree(payload);
            EventEnvelope envelope = new EventEnvelope(entity, payloadNode);
            return publishEventToTopic(eventsTopic, key, envelope);
        } catch (Exception e) {
            log.error("Failed to create EventEnvelope for entity {} key {}", entity, key, e);
            CompletableFuture<SendResult<String, EventEnvelope>> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    /**
     * Publish a tombstone to the per-entity aggregates topic for the given entity/key.
     * This ensures the per-entity aggregate listeners receive the tombstone and can delete their materialized view.
     *
     * @return a future that completes when the send succeeds/fails
     */
    public CompletableFuture<SendResult<String, EventEnvelope>> publishDeleteForEntity(String entity, String key) {
        String topic = ENTITY_TOPICS.get(entity);
        if (topic == null) {
            throw new IllegalArgumentException("Unknown entity type: " + entity);
        }
        return publishDeleteToTopic(topic, key);
    }

    /**
     * Publish a typed event to the specified topic using the provided key.
     *
     * @return a future that completes when the send succeeds/fails
     */
    private CompletableFuture<SendResult<String, EventEnvelope>> publishEventToTopic(
            String topic, String key, EventEnvelope value) {
        return kafkaTemplate
                .send(topic, key, value)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event to topic {} for key {}", topic, key, ex);
                    } else if (result != null) {
                        log.debug(
                                "Published event to topic {} partition {} offset {} for key {}",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                key);
                    } else {
                        log.debug("Published event to topic {} for key {} (no metadata)", topic, key);
                    }
                    // increment metrics on successful publish
                    if (ex == null) {
                        eventsPublishedCounter.increment();
                        // also emit a per-topic counter with tag "topic"
                        meterRegistry
                                .counter("app.kafka.events.published", "topic", topic)
                                .increment();
                    }
                })
                .toCompletableFuture();
    }

    /**
     * Publish a tombstone (null value) for the provided key on the specified topic.
     *
     * @return a future that completes when the send succeeds/fails
     */
    private CompletableFuture<SendResult<String, EventEnvelope>> publishDeleteToTopic(String topic, String key) {
        return kafkaTemplate
                .send(topic, key, null)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish delete (tombstone) to topic {} for key {}", topic, key, ex);
                    } else {
                        // reference result to avoid unused-parameter warnings (log at trace level)
                        if (result != null) {
                            log.trace("Tombstone send result: {}", result);
                        }
                        log.debug("Published delete (tombstone) to topic {} for key {}", topic, key);
                        tombstonesPublishedCounter.increment();
                        meterRegistry
                                .counter("app.kafka.tombstones.published", "topic", topic)
                                .increment();
                    }
                })
                .toCompletableFuture();
    }
}
