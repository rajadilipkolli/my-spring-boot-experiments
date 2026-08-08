package com.example.ultimateredis.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;

@ExtendWith(MockitoExtension.class)
class RedisValueOperationsUtilTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

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
    void getKeysWithPattern_shouldReturnMatchingKeys() throws Exception {
        // Arrange
        String pattern = "test*";
        RedisConnection connection = org.mockito.Mockito.mock(RedisConnection.class);
        RedisKeyCommands keyCommands = org.mockito.Mockito.mock(RedisKeyCommands.class);
        Cursor<byte[]> cursor = org.mockito.Mockito.mock(Cursor.class);
        RedisSerializer<?> serializer = org.mockito.Mockito.mock(RedisSerializer.class);

        when(connection.keyCommands()).thenReturn(keyCommands);
        when(keyCommands.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("test1".getBytes(), "test2".getBytes());
        when(redisTemplate.getKeySerializer()).thenReturn((RedisSerializer) serializer);
        when(serializer.deserialize(any(byte[].class))).thenReturn("test1", "test2");

        org.mockito.ArgumentCaptor<RedisCallback<Set<String>>> captor =
                org.mockito.ArgumentCaptor.forClass(RedisCallback.class);

        when(redisTemplate.execute(captor.capture())).thenAnswer(invocation -> {
            RedisCallback<Set<String>> callback = invocation.getArgument(0);
            return callback.doInRedis(connection);
        });

        // Act
        Set<String> result = redisValueOpsUtil.getKeysWithPattern(pattern);

        // Assert
        assertThat(result).containsExactlyInAnyOrder("test1", "test2");
        verify(redisTemplate).execute(any(RedisCallback.class));
        verify(cursor).close();
    }

    @Test
    void deleteByPattern_withMatchingKeys_shouldDeleteThem() {
        // Arrange
        String pattern = "test*";
        Set<String> matchingKeys = Set.of("test1", "test2");
        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(matchingKeys);

        // Act
        redisValueOpsUtil.deleteByPattern(pattern);

        // Assert
        verify(redisTemplate).execute(any(RedisCallback.class));
        verify(redisTemplate).delete(matchingKeys);
    }

    @Test
    void deleteByPattern_withNoMatchingKeys_shouldNotCallDelete() {
        // Arrange
        String pattern = "test*";
        Set<String> emptySet = Collections.emptySet();
        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(emptySet);

        // Act
        redisValueOpsUtil.deleteByPattern(pattern);

        // Assert
        verify(redisTemplate).execute(any(RedisCallback.class));
        verify(redisTemplate, never()).delete(emptySet);
    }
}
