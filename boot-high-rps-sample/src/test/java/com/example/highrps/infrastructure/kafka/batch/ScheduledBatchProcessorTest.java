package com.example.highrps.infrastructure.kafka.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ScheduledBatchProcessorTest {

    @Test
    void shouldCreateConsumerNameFromHostnameAndUuid() {
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        String consumerName = ScheduledBatchProcessor.createConsumerName("my-host", uuid);

        assertEquals("processor-my-host-123e4567-e89b-12d3-a456-426614174000", consumerName);
    }
}
