package com.example.ultimateredis.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisRateLimiterTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;

    @Mock private RedisScriptExecutor scriptExecutor;
    @Mock private ValueOperations<String, Object> valueOperations;

    private RedisRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rateLimiter = new RedisRateLimiter(redisTemplate, scriptExecutor);
    }

    @Test
    void tryAcquire_shouldExecuteTokenBucketScript() {
        // Arrange
        String key = "test-key";
        double tokensPerSecond = 10.0;
        int burstCapacity = 20;

        // Use lenient stubbing to avoid "unnecessary stubbing" warnings
        when(scriptExecutor.executeScript(
                        anyString(),
                        eq(Boolean.class),
                        any(List.class),
                        any(Double.class),
                        any(Integer.class),
                        any(Double.class),
                        any(Integer.class)))
                .thenReturn(true);

        // Act
        boolean result = rateLimiter.tryAcquire(key, tokensPerSecond, burstCapacity);

        // Assert
        assertThat(result).isTrue();

        // Verify script execution with correct parameters
        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        verify(scriptExecutor)
                .executeScript(
                        anyString(),
                        eq(Boolean.class),
                        keysCaptor.capture(),
                        eq(tokensPerSecond),
                        eq(burstCapacity),
                        any(Double.class),
                        eq(1));

        // Check keys format: "rate-limiter:test-key:tokens" and "rate-limiter:test-key:timestamp"
        List<String> capturedKeys = keysCaptor.getValue();
        assertThat(capturedKeys).hasSize(2);
        assertThat(capturedKeys.get(0)).isEqualTo("rate-limiter:test-key:tokens");
        assertThat(capturedKeys.get(1)).isEqualTo("rate-limiter:test-key:timestamp");
    }

    @Test
    void tryAcquire_shouldReturnFalseWhenRateLimitExceeded() {
        // Arrange
        String key = "test-key";
        double tokensPerSecond = 5.0;
        int burstCapacity = 10;

        // Use more generic matchers
        when(scriptExecutor.executeScript(
                        anyString(),
                        eq(Boolean.class),
                        any(List.class),
                        any(Double.class),
                        any(Integer.class),
                        any(Double.class),
                        any(Integer.class)))
                .thenReturn(false);

        // Act
        boolean result = rateLimiter.tryAcquire(key, tokensPerSecond, burstCapacity);

        // Assert
        assertThat(result).isFalse();

        // Verify the call was made with correct parameters
        verify(scriptExecutor)
                .executeScript(
                        anyString(),
                        eq(Boolean.class),
                        any(List.class),
                        eq(tokensPerSecond),
                        eq(burstCapacity),
                        any(Double.class),
                        eq(1));
    }

    @Test
    void tryAcquireWithFixedWindow_shouldReturnTrueWhenBelowLimit() {
        // Arrange
        String key = "test-fixed-key";
        int maxOperations = 5;
        Duration windowDuration = Duration.ofMinutes(1);

        // First request in window gets count 1
        when(valueOperations.increment(anyString(), eq(1L))).thenReturn(1L);

        // Act
        boolean result = rateLimiter.tryAcquireWithFixedWindow(key, maxOperations, windowDuration);

        // Assert
        assertThat(result).isTrue();

        // Verify expiry is set on first request
        verify(redisTemplate).expire(eq("rate-limiter:fixed:test-fixed-key"), eq(windowDuration));
    }

    @Test
    void tryAcquireWithFixedWindow_shouldReturnFalseWhenLimitExceeded() {
        // Arrange
        String key = "test-fixed-key";
        int maxOperations = 5;
        Duration windowDuration = Duration.ofMinutes(1);

        // Return count higher than limit
        when(valueOperations.increment(anyString(), eq(1L))).thenReturn(6L);

        // Act
        boolean result = rateLimiter.tryAcquireWithFixedWindow(key, maxOperations, windowDuration);

        // Assert
        assertThat(result).isFalse();

        // Verify expiry is NOT set (not first request)
        verify(redisTemplate, times(0)).expire(any(), any(Duration.class));
    }
}
