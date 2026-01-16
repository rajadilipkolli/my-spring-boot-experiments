package com.example.highrps.service;

import com.example.highrps.model.request.NewPostRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, NewPostRequest> kafkaTemplate;

    @Value("${app.kafka.events-topic:events}")
    private String eventsTopic;

    public KafkaProducerService(KafkaTemplate<String, NewPostRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEvent(NewPostRequest newPostRequest) {
        kafkaTemplate.send(eventsTopic, newPostRequest.title(), newPostRequest).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event with id: {}", newPostRequest.title(), ex);
            } else {
                log.debug(
                        "Successfully published event with id: {} to partition: {}",
                        newPostRequest.title(),
                        result.getRecordMetadata().partition());
            }
        });
    }

    public void publishDelete(String title) {
        // Send a tombstone (null value) for the given key so Kafka Streams reduces it out
        kafkaTemplate.send(eventsTopic, title, null).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish delete (tombstone) for id: {}", title, ex);
            } else {
                log.debug("Published delete (tombstone) for id: {}", title);
            }
        });
    }
}
