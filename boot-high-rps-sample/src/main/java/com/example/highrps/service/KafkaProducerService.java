package com.example.highrps.service;

import com.example.highrps.repository.EventDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, EventDto> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, EventDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEvent(EventDto event) {
        kafkaTemplate.send("events", event.getId(), event);
    }
}
