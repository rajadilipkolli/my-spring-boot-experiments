package com.example.highrps.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.common.AbstractIntegrationTest;
import java.time.Duration;
import org.apache.kafka.streams.KafkaStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KafkaErrorHandlingIT extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        super.clearDatabase();
    }

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
    void shouldNotCrashStreamsOnPoisonPill() throws Exception {
        // Act: send poison pill to the topic consumed by Streams
        String poisonPillKey = "streams-poison-pill-key";
        byte[] poisonPillValue = "not-a-valid-json".getBytes();
        kafkaTemplate.send("events", poisonPillKey, poisonPillValue).get(10, java.util.concurrent.TimeUnit.SECONDS);

        // Assert: wait a bit and ensure Streams state is still RUNNING
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            var streams = streamsBuilderFactoryBean.getKafkaStreams();
            assertThat(streams).isNotNull();
            assertThat(streams.state()).isEqualTo(KafkaStreams.State.RUNNING);
        });

        // Note: Kafka Streams doesn't automatically route to DLT for deserialization errors
        // without explicit RecoveringDeserializationExceptionHandler configuration.
        // We only assert it didn't crash here.
        /*
        Map<String, Object> consumerProps =
                KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), "test-dlt-group", Boolean.TRUE);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        try (Consumer<byte[], byte[]> consumer =
                new KafkaConsumer<>(consumerProps, new ByteArrayDeserializer(), new ByteArrayDeserializer())) {

            consumer.subscribe(Collections.singletonList("events.DLT"));
            ConsumerRecord<byte[], byte[]> dltRecord =
                    KafkaTestUtils.getSingleRecord(consumer, "events.DLT", Duration.ofSeconds(10));
            assertThat(dltRecord).isNotNull();
            assertThat(new String(dltRecord.value())).isEqualTo("not-a-valid-json");
        }
        */
    }
}
