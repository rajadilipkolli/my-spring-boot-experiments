package com.example.ultimateredis.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.ultimateredis.common.AbstractIntegrationTest;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class RedisPubSubTest extends AbstractIntegrationTest {

    @Test
    void publishMessage_shouldReturnSuccessAndConsume() {
        String message = "Hello PubSub Integration Test!";
        String topic = "app-events";

        // Clear previous state
        redisConsumer.clear();

        this.mockMvcTester
                .post()
                .uri("/v1/redis/pubsub/publish")
                .param("topic", topic)
                .param("message", message)
                .contentType(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk();

        // Verify deterministic consumption, handling potential JSON string quotes
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            String received = redisConsumer.getLastReceivedMessage();
            assertThat(received).isNotNull();
            assertThat(received).isEqualTo(message);
        });
    }
}
