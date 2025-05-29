package com.example.ultimateredis.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisValueOperationsUtilTest {

    @Mock private RedisTemplate<String, String> redisTemplate;

    @Mock private ValueOperations<String, String> valueOperations;

    private RedisValueOperationsUtil<String> redisValueOpsUtil;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        redisValueOpsUtil = new RedisValueOperationsUtil<>(redisTemplate);
    }

    @Test
    void putValue_shouldSetKeyValue() {
        // Arrange
        String key = "test-key";
        String value = "test-value";

        // Act
        redisValueOpsUtil.putValue(key, value);

        // Assert
        verify(valueOperations).set(key, value);
    }

    @Test
    void getValue_shouldReturnValue() {
        // Arrange
        String key = "test-key";
        String expectedValue = "test-value";
        when(valueOperations.get(key)).thenReturn(expectedValue);

        // Act
        String result = redisValueOpsUtil.getValue(key);

        // Assert
        assertThat(result).isEqualTo(expectedValue);
        verify(valueOperations).get(key);
    }

    @Test
    void setExpire_shouldSetExpiry() {
        // Arrange
        String key = "test-key";
        long timeout = 30;
        TimeUnit unit = TimeUnit.MINUTES;

        // Act
        redisValueOpsUtil.setExpire(key, timeout, unit);

        // Assert
        verify(redisTemplate).expire(key, timeout, unit);
    }

    @Test
    void getKeysWithPattern_shouldReturnMatchingKeys() {
        // Arrange
        String pattern = "test*";
        Set<String> expectedKeys = Set.of("test1", "test2", "test3");
        when(redisTemplate.keys(pattern)).thenReturn(expectedKeys);

        // Act
        Set<String> result = redisValueOpsUtil.getKeysWithPattern(pattern);

        // Assert
        assertThat(result).isEqualTo(expectedKeys);
        verify(redisTemplate).keys(pattern);
    }

    @Test
    void deleteByPattern_withMatchingKeys_shouldDeleteThem() {
        // Arrange
        String pattern = "test*";
        Set<String> matchingKeys = Set.of("test1", "test2");
        when(redisTemplate.keys(pattern)).thenReturn(matchingKeys);

        // Act
        redisValueOpsUtil.deleteByPattern(pattern);

        // Assert
        verify(redisTemplate).keys(pattern);
        verify(redisTemplate).delete(matchingKeys);
    }

    @Test
    void deleteByPattern_withNoMatchingKeys_shouldNotCallDelete() {
        // Arrange
        String pattern = "test*";
        Set<String> emptySet = Collections.emptySet();
        when(redisTemplate.keys(pattern)).thenReturn(emptySet);

        // Act
        redisValueOpsUtil.deleteByPattern(pattern);

        // Assert
        verify(redisTemplate).keys(pattern);
        verify(redisTemplate, never()).delete(emptySet);
    }
}
