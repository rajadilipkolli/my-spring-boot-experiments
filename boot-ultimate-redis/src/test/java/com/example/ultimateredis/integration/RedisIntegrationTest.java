package com.example.ultimateredis.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ultimateredis.common.TestcontainersConfiguration;
import com.example.ultimateredis.config.RedisRateLimiter;
import com.example.ultimateredis.config.RedisScriptExecutor;
import com.example.ultimateredis.config.RedisValueOperationsUtil;
import com.example.ultimateredis.model.AddRedisRequest;
import com.example.ultimateredis.service.RedisService;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(classes = {TestcontainersConfiguration.class})
class RedisIntegrationTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:6.2-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired private RedisTemplate<String, Object> redisTemplate;

    @Autowired private RedisValueOperationsUtil<String> redisValueOpsUtil;

    @Autowired private RedisService redisService;

    @Autowired private RedisScriptExecutor scriptExecutor;

    @Autowired private RedisRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        // Clear Redis before each test
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
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
    void redisValueOperations_withExpiry_shouldExpireAfterTime() throws Exception {
        // Arrange
        String key = "expiring-key";
        String value = "expiring-value";

        // Act
        redisValueOpsUtil.putValue(key, value);
        redisValueOpsUtil.setExpire(key, 1, java.util.concurrent.TimeUnit.SECONDS);

        // Assert - key exists now
        assertThat(redisValueOpsUtil.getValue(key)).isEqualTo(value);

        // Wait for expiry
        Thread.sleep(1500);

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

        // Assert
        assertThat(keys).hasSize(3);
        assertThat(keys).contains("test:1", "test:2", "test:3");

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
        assertThat(firstValue).isEqualTo(1L);
        assertThat(secondValue).isEqualTo(2L);
        assertThat(thirdValue).isEqualTo(3L);
        assertThat(fourthValue).isEqualTo(4L);
        assertThat(fifthValue).isEqualTo(1L); // Wrapped around to min value
        assertThat(sixthValue).isEqualTo(2L);
    }

    @Test
    void redisRateLimiter_shouldLimitRequests() {
        // Arrange
        String key = "rate-limit-test";
        int maxOperations = 3;
        Duration window = Duration.ofMinutes(1);

        // Act & Assert - first three requests allowed
        assertThat(rateLimiter.tryAcquireWithFixedWindow(key, maxOperations, window)).isTrue();
        assertThat(rateLimiter.tryAcquireWithFixedWindow(key, maxOperations, window)).isTrue();
        assertThat(rateLimiter.tryAcquireWithFixedWindow(key, maxOperations, window)).isTrue();

        // 4th request should be denied
        assertThat(rateLimiter.tryAcquireWithFixedWindow(key, maxOperations, window)).isFalse();
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
