package com.example.highrps.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.common.AbstractIntegrationTest;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KafkaErrorHandlingIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should route poison pill from consumer to DLT and save to Redis")
    void shouldRoutePoisonPillToDLT() {
        // Arrange
        String topic = "posts-aggregates";
        String retryableTopicDlt = topic + "-dlt";
        String dlqKey = "dlq:" + retryableTopicDlt;

        // Act: send poison pill
        String poisonPillKey = "poison-pill-key";
        byte[] poisonPillValue = "invalid-json-not-base64".getBytes();
        kafkaTemplate.send(topic, poisonPillKey, poisonPillValue);

        // Assert: verify the poison pill arrives in DLT and is written to Redis by @DltHandler
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Long size = redisTemplate.opsForList().size(dlqKey);
            assertThat(size).isNotNull().isGreaterThanOrEqualTo(1L);
        });
    }

    @Test
    @DisplayName("Should not crash Streams application when poison pill is encountered")
    void shouldNotCrashStreamsOnPoisonPill() {
        // Act: send poison pill to the topic consumed by Streams
        String poisonPillKey = "streams-poison-pill-key";
        byte[] poisonPillValue = "not-a-valid-json".getBytes();
        kafkaTemplate.send("authors-aggregates", poisonPillKey, poisonPillValue);

        // Assert: wait a bit and ensure Streams state is still RUNNING
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            var streams = streamsBuilderFactoryBean.getKafkaStreams();
            assertThat(streams).isNotNull();
            assertThat(streams.state()).isEqualTo(org.apache.kafka.streams.KafkaStreams.State.RUNNING);
        });
    }
}
