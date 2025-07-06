package com.example.ultimateredis.config;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

@ExtendWith(MockitoExtension.class)
class RedisMetricsAspectTest {

    @Mock private ProceedingJoinPoint joinPoint;

    @Mock private Signature signature;

    private MeterRegistry meterRegistry;

    private RedisMetricsAspect aspect;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        aspect = new RedisMetricsAspect(meterRegistry);

        // Use lenient() to avoid unnecessary stubbing warnings
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(signature.getName()).thenReturn("testMethod");
    }

    @Test
    void measureRedisOperationTime_shouldRecordSuccessfulOperation() throws Throwable {
        // Arrange
        when(joinPoint.proceed()).thenReturn("success");

        // Act
        Object result = aspect.measureRedisOperationTime(joinPoint);

        // Assert
        // Check timer was created
        Timer timer = meterRegistry.find("redis.operation").tag("method", "testMethod").timer();
        assert timer != null;
        assert timer.count() == 1;

        // Check operation counter was incremented
        Counter counter =
                meterRegistry
                        .find("redis.operations")
                        .tag("method", "testMethod")
                        .tag("outcome", "success")
                        .counter();
        assert counter != null;
        assert counter.count() == 1;

        // Error counter should not be incremented
        assert meterRegistry.find("redis.errors").counter() == null;
    }

    @Test
    void measureRedisOperationTime_shouldRecordFailedOperation() throws Throwable {
        // Arrange
        RuntimeException exception = new RuntimeException("Redis error");
        when(joinPoint.proceed()).thenThrow(exception);

        // Act
        try {
            aspect.measureRedisOperationTime(joinPoint);
        } catch (Throwable e) {
            // Expected exception
        }

        // Assert
        // Check timer was created
        Timer timer = meterRegistry.find("redis.operation").tag("method", "testMethod").timer();
        assert timer != null;
        assert timer.count() == 1;

        // Check operation counter was incremented with failure outcome
        Counter opsCounter =
                meterRegistry
                        .find("redis.operations")
                        .tag("method", "testMethod")
                        .tag("outcome", "failure")
                        .counter();
        assert opsCounter != null;
        assert opsCounter.count() == 1;

        // Check error counter was incremented
        Counter errorCounter =
                meterRegistry.find("redis.errors").tag("method", "testMethod").counter();
        assert errorCounter != null;
        assert errorCounter.count() == 1;
    }

    @Test
    void redisServiceMethodsPointcut_shouldApplyToRedisServiceMethods() {
        // This test verifies that the pointcut expression correctly matches RedisService methods

        // Create a proxy factory
        AspectJProxyFactory factory = new AspectJProxyFactory(new TestRedisService());

        // Add the aspect to the factory
        RedisMetricsAspect testAspect = new RedisMetricsAspect(meterRegistry);
        factory.addAspect(testAspect);

        // Create the proxy
        TestRedisService proxy = factory.getProxy();

        // Call a method on the proxy that should be intercepted
        proxy.getValue("test-key");

        // There should be a timer recorded for this method call
        Timer timer = meterRegistry.find("redis.operation").tag("method", "getValue").timer();
        assert timer != null;
        assert timer.count() == 1;
    } // Test implementation of RedisService for aspect testing

    // Needs to be in com.example.ultimateredis.service package to match the pointcut
    static class TestRedisService {
        public String getValue(String key) {
            return "test-value";
        }
    }
}
