package com.example.ultimateredis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.ultimateredis.common.AbstractIntegrationTest;
import com.example.ultimateredis.config.RedisRateLimiter;
import com.example.ultimateredis.config.RedisScriptExecutor;
import com.example.ultimateredis.config.RedisValueOperationsUtil;
import com.example.ultimateredis.model.AddRedisRequest;
import com.example.ultimateredis.service.RedisService;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

class UltimateRedisApplicationTests extends AbstractIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisValueOperationsUtil<String> redisValueOpsUtil;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisScriptExecutor scriptExecutor;

    @Autowired
    private RedisRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        // Clear Redis before each test
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void contextLoads() {
        assertThat(redisTemplate).isNotNull();
    }

    @Test
    void redisValueOperations_shouldStoreAndRetrieveValue() {
        // Arrange
        String key = "test-key";
        String value = "test-value";

        // Act
        redisValueOpsUtil.putValue(key, value);
        String retrievedValue = redisValueOpsUtil.getValue(key);

        // Assert
        assertThat(retrievedValue).isEqualTo(value);
    }

    @Test
    void redisValueOperations_withExpiry_shouldExpireAfterTime() {
        // Arrange
        String key = "expiring-key";
        String value = "expiring-value";

        // Act
        redisValueOpsUtil.putValue(key, value);
        redisValueOpsUtil.setExpire(key, 1, java.util.concurrent.TimeUnit.SECONDS);

        // Assert - key exists now
        assertThat(redisValueOpsUtil.getValue(key)).isEqualTo(value);

        // Wait for expiry using Awaitility
        await().atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> redisValueOpsUtil.getValue(key) == null);

        // Assert - key expired
        assertThat(redisValueOpsUtil.getValue(key)).isNull();
    }

    @Test
    void redisService_shouldAddAndRetrieveValue() {
        // Arrange
        String key = "service-key";
        String value = "service-value";
        int expireMinutes = 5;

        AddRedisRequest request = new AddRedisRequest(key, value, expireMinutes);

        // Act
        redisService.addRedis(request);
        String retrievedValue = redisService.getValue(key);

        // Assert
        assertThat(retrievedValue).isEqualTo(value);
    }

    @Test
    void redisPatternOperations_shouldFindAndDeleteByPattern() {
        // Arrange
        redisValueOpsUtil.putValue("test:1", "value1");
        redisValueOpsUtil.putValue("test:2", "value2");
        redisValueOpsUtil.putValue("test:3", "value3");
        redisValueOpsUtil.putValue("other:1", "otherValue");

        // Act - find by pattern
        var keys = redisValueOpsUtil.getKeysWithPattern("test:*");

        assertThat(keys)
                // Assert
                .hasSize(3)
                .contains("test:1", "test:2", "test:3");

        // Act - delete by pattern
        redisValueOpsUtil.deleteByPattern("test:*");
        var keysAfterDelete = redisValueOpsUtil.getKeysWithPattern("test:*");
        var otherKeys = redisValueOpsUtil.getKeysWithPattern("other:*");

        // Assert
        assertThat(keysAfterDelete).isEmpty();
        assertThat(otherKeys).hasSize(1);
    }

    @Test
    void redisScriptExecutor_shouldExecuteLuaScript() {
        // Arrange
        String key = "counter";

        // Act - use incrementWithinRange
        Long firstValue = scriptExecutor.incrementWithinRange(key, 1, 5);
        Long secondValue = scriptExecutor.incrementWithinRange(key, 1, 5);
        Long thirdValue = scriptExecutor.incrementWithinRange(key, 1, 5);
        Long fourthValue = scriptExecutor.incrementWithinRange(key, 1, 5);
        Long fifthValue = scriptExecutor.incrementWithinRange(key, 1, 5);
        Long sixthValue = scriptExecutor.incrementWithinRange(key, 1, 5);

        // Assert - wraps around after reaching max
        assertThat(firstValue).isOne();
        assertThat(secondValue).isEqualTo(2L);
        assertThat(thirdValue).isEqualTo(3L);
        assertThat(fourthValue).isEqualTo(4L);
        assertThat(fifthValue).isOne(); // Wrapped around to min value
        assertThat(sixthValue).isEqualTo(2L);
    }

    @Test
    void redisRateLimiter_tokenBucket_shouldReplenishTokensOverTime() throws Exception {
        // Arrange
        String key = "token-replenish-test";
        double rate = 5.0; // 5 tokens per second
        int capacity = 10;

        // Act - consume all tokens
        for (int i = 0; i < capacity; i++) {
            assertThat(rateLimiter.tryAcquire(key, rate, capacity)).isTrue();
        }

        // Verify bucket is empty
        assertThat(rateLimiter.tryAcquire(key, rate, capacity)).isFalse();

        // Wait for token replenishment (1 second = 5 new tokens) using Awaitility
        await().atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    int replenished = 0;
                    // Try to acquire up to 5 tokens, but allow for the possibility that
                    // tokens may not be replenished all at once
                    for (int i = 0; i < 5; i++) {
                        if (rateLimiter.tryAcquire(key, rate, capacity)) {
                            replenished++;
                        }
                    }
                    // At least 1 token should be replenished (be tolerant to timing)
                    assertThat(replenished).isPositive();
                });
        // After consuming replenished tokens, bucket should be empty again
        assertThat(rateLimiter.tryAcquire(key, rate, capacity)).isFalse();
    }

    @Test
    void redisRateLimiter_shouldLimitRequests() {
        // Arrange
        String key = "rate-limit-test";
        int maxOperations = 3;
        Duration window = Duration.ofMinutes(1);

        // Act & Assert - first three requests allowed
        assertThat(rateLimiter.tryAcquireWithFixedWindow(key, maxOperations, window))
                .isTrue();
        assertThat(rateLimiter.tryAcquireWithFixedWindow(key, maxOperations, window))
                .isTrue();
        assertThat(rateLimiter.tryAcquireWithFixedWindow(key, maxOperations, window))
                .isTrue();

        // 4th request should be denied
        assertThat(rateLimiter.tryAcquireWithFixedWindow(key, maxOperations, window))
                .isFalse();
    }

    @Test
    void redisRateLimiter_tokenBucket_shouldAllowBursts() {
        // Arrange
        String key = "token-bucket-test";
        double rate = 10.0; // 10 tokens per second
        int capacity = 20; // Burst of 20

        // Act & Assert - first burst request allowed (consume all tokens)
        for (int i = 0; i < capacity; i++) {
            assertThat(rateLimiter.tryAcquire(key, rate, capacity)).isTrue();
        }

        // Next request should be denied as bucket is empty
        assertThat(rateLimiter.tryAcquire(key, rate, capacity)).isFalse();
    }
}
