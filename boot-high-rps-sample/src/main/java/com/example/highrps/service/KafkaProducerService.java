package com.example.highrps.service;

import com.example.highrps.model.request.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final JsonMapper mapper;

    @Value("${app.kafka.events-topic:events}")
    private String eventsTopic;

    @SuppressWarnings({"unchecked"})
    public KafkaProducerService(KafkaTemplate<String, ?> kafkaTemplate, JsonMapper mapper) {
        // cast to KafkaTemplate<String, Object> so we can send arbitrary payload types
        this.kafkaTemplate = (KafkaTemplate<String, Object>) kafkaTemplate;
        this.mapper = mapper;
    }

    /**
     * Publish an EventEnvelope to the default events topic. This wraps the payload as JSON
     * and adds an entity label so Streams branching can route to the right per-entity topic.
     */
    public void publishEnvelope(String entity, String key, Object payload) {
        try {
            JsonNode payloadNode = mapper.valueToTree(payload);
            EventEnvelope envelope = new EventEnvelope(entity, payloadNode);
            publishEventToTopic(eventsTopic, key, envelope);
        } catch (Exception e) {
            log.error("Failed to create EventEnvelope for entity {} key {}", entity, key, e);
        }
    }

    /**
     * Publish a tombstone to the per-entity aggregates topic for the given entity/key.
     * This ensures the per-entity aggregate listeners receive the tombstone and can delete their materialized view.
     */
    public void publishDeleteForEntity(String entity, String key) {
        String topic =
                entity + "s-aggregates"; // simple pluralization: post -> posts-aggregates, author -> authors-aggregates
        publishDeleteToTopic(topic, key);
    }

    /**
     * Publish a typed event to the specified topic using the provided key.
     */
    public void publishEventToTopic(String topic, String key, Object value) {
        kafkaTemplate.send(topic, key, value).whenComplete((result, ex) -> {
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
        });
    }

    /**
     * Publish a tombstone (null value) for the provided key on the specified topic.
     */
    public void publishDeleteToTopic(String topic, String key) {
        kafkaTemplate.send(topic, key, null).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish delete (tombstone) to topic {} for key {}", topic, key, ex);
            } else {
                log.debug("Published delete (tombstone) to topic {} for key {}", topic, key);
            }
        });
    }
}
