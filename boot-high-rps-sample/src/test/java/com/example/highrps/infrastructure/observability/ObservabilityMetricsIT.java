package com.example.highrps.infrastructure.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.post.domain.PostDetailsResponse;
import com.example.highrps.post.domain.events.PostCreatedEvent;
import com.example.highrps.shared.IdGenerator;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class ObservabilityMetricsIT extends AbstractIntegrationTest {

    /** Verifies Kafka metric publication and correlation identifier propagation through MDC. */
    @Test
    @DisplayName("Should export Kafka client metrics and propagate correlationId via MDC")
    void shouldExportKafkaMetricsAndMDC() throws Exception {
        String correlationId = "observability-corr-id";
        MDC.put("correlationId", correlationId);

        try {
            LocalDateTime now = LocalDateTime.now();
            PostCreatedEvent event = new PostCreatedEvent(
                    IdGenerator.generateLong(),
                    "Observability test",
                    "Valid aggregate payload",
                    "observability@example.com",
                    false,
                    null,
                    now,
                    new PostDetailsResponse("observability", now, "integration-test"),
                    List.of());

            // Trigger producer action which will fire the MdcProducerInterceptor
            SendResult<String, Object> sendResult = kafkaTemplate
                    .send("posts-aggregates", String.valueOf(event.postId()), event)
                    .get(5, TimeUnit.SECONDS);

            KafkaTemplate<String, Object> localKafkaTemplate = new KafkaTemplate<>(producerFactory);
            localKafkaTemplate.setConsumerFactory(
                    applicationContext.getBean("newPostConsumerFactory", ConsumerFactory.class));
            ConsumerRecord<String, Object> record = localKafkaTemplate.receive(
                    "posts-aggregates",
                    sendResult.getRecordMetadata().partition(),
                    sendResult.getRecordMetadata().offset(),
                    Duration.ofSeconds(5));

            assertThat(record).isNotNull();
            assertThat(record.headers().lastHeader("correlationId")).isNotNull();
            assertThat(new String(record.headers().lastHeader("correlationId").value()))
                    .isEqualTo(correlationId);

            await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                // Assert that Kafka client metrics are registered
                // Note: Spring Kafka automatically configures micrometer metrics if enabled
                boolean hasKafkaMetrics = meterRegistry.getMeters().stream()
                        .anyMatch(m -> m.getId().getName().startsWith("kafka.producer"));
                assertThat(hasKafkaMetrics).isTrue();
            });
        } finally {
            MDC.remove("correlationId");
        }
    }
}
