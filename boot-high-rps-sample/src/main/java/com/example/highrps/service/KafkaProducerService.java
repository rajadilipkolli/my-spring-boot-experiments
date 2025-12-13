package com.example.highrps.service;

import com.example.highrps.model.EventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, EventDto> kafkaTemplate;

    @Value("${app.kafka.events-topic:events}")
    private String eventsTopic;

    public KafkaProducerService(KafkaTemplate<String, EventDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEvent(EventDto event) {
        kafkaTemplate.send(eventsTopic, event.getId(), event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event with id: {}", event.getId(), ex);
            } else {
                log.debug(
                        "Successfully published event with id: {} to partition: {}",
                        event.getId(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}
