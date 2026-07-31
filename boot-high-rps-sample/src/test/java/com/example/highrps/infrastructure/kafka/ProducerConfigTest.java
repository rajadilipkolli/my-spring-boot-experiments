package com.example.highrps.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.highrps.common.AbstractIntegrationTest;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;

class ProducerConfigTest extends AbstractIntegrationTest {

    @Test
    void producerConfigShouldBeHardened() {
        Map<String, Object> producerProperties =
                kafkaTemplate.getProducerFactory().getConfigurationProperties();

        assertThat(producerProperties.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(String.valueOf(producerProperties.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)))
                .isEqualTo("true");
        assertThat(Integer.parseInt(
                        String.valueOf(producerProperties.get(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION))))
                .isLessThanOrEqualTo(5);
        assertThat(String.valueOf(producerProperties.get(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG)))
                .isEqualTo("120000");
        assertThat(String.valueOf(producerProperties.get(ProducerConfig.RETRY_BACKOFF_MS_CONFIG)))
                .isEqualTo("100");
    }
}
