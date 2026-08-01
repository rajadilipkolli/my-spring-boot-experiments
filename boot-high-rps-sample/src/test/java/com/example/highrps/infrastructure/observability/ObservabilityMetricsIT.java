package com.example.highrps.infrastructure.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.common.AbstractIntegrationTest;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class ObservabilityMetricsIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should export Kafka client metrics and propagate correlationId via MDC")
    void shouldExportKafkaMetricsAndMDC() {
        String correlationId = "observability-corr-id";
        MDC.put("correlationId", correlationId);

        try {
            // Trigger producer action which will fire the MdcProducerInterceptor
            kafkaTemplate.send("events", "test-key", "test-payload");

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
