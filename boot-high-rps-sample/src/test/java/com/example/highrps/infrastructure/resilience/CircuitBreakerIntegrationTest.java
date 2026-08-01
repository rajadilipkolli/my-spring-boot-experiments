package com.example.highrps.infrastructure.resilience;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.highrps.common.AbstractIntegrationTest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CircuitBreakerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    public void testCircuitBreakersAreConfigured() {
        var breakers = circuitBreakerRegistry.getAllCircuitBreakers();
        var names = breakers.stream().map(CircuitBreaker::getName).toList();

        assertThat(names).contains("dbBatchWrites", "redisProjection");

        var dbBatchWrites = breakers.stream()
                .filter(cb -> "dbBatchWrites".equals(cb.getName()))
                .findFirst()
                .get();
        assertThat(dbBatchWrites.getCircuitBreakerConfig().getSlidingWindowSize())
                .isEqualTo(10);
        assertThat(dbBatchWrites.getCircuitBreakerConfig().getFailureRateThreshold())
                .isEqualTo(50.0f);

        var redisProjection = breakers.stream()
                .filter(cb -> "redisProjection".equals(cb.getName()))
                .findFirst()
                .get();
        assertThat(redisProjection.getCircuitBreakerConfig().getSlidingWindowSize())
                .isEqualTo(10);
        assertThat(redisProjection.getCircuitBreakerConfig().getFailureRateThreshold())
                .isEqualTo(50.0f);
    }
}
