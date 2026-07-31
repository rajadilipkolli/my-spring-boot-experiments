package com.example.highrps.infrastructure.resilience;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.infrastructure.kafka.batch.ScheduledBatchProcessor;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
        properties = {"spring.kafka.consumer.auto-offset-reset=earliest", "spring.kafka.listener.auto-startup=false"})
public class CircuitBreakerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private ScheduledBatchProcessor scheduledBatchProcessor;

    @Test
    public void testCircuitBreakersAreConfigured() {
        assertThat(circuitBreakerRegistry.circuitBreaker("dbBatchWrites")).isNotNull();
        assertThat(circuitBreakerRegistry.circuitBreaker("redisProjection")).isNotNull();
    }
}
