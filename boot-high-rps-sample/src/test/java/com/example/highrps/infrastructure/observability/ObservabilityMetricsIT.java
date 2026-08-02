package com.example.highrps.infrastructure.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.common.AbstractIntegrationTest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class ObservabilityMetricsIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should export Kafka client metrics and propagate correlationId via MDC")
    void shouldExportKafkaMetricsAndMDC() throws Exception {
        String correlationId = "observability-corr-id";
        MDC.put("correlationId", correlationId);

        try {
            // Trigger producer action which will fire the MdcProducerInterceptor
            SendResult<String, Object> sendResult =
                    kafkaTemplate.send("events", "test-key", "test-payload").get(5, TimeUnit.SECONDS);

            KafkaTemplate<String, Object> localKafkaTemplate = new KafkaTemplate<>(producerFactory);
            localKafkaTemplate.setConsumerFactory(
                    applicationContext.getBean("newPostConsumerFactory", ConsumerFactory.class));
            ConsumerRecord<String, Object> record = localKafkaTemplate.receive(
                    "events",
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
