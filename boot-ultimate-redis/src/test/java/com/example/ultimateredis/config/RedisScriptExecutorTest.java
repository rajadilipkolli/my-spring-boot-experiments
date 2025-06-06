package com.example.ultimateredis.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

@ExtendWith(MockitoExtension.class)
class RedisScriptExecutorTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;

    private RedisScriptExecutor scriptExecutor;

    @Captor private ArgumentCaptor<RedisScript<Object>> scriptCaptor;

    @BeforeEach
    void setUp() {
        scriptExecutor = new RedisScriptExecutor(redisTemplate);
    }

    @Test
    void executeScript_shouldExecuteRedisScriptWithReturnType() {
        // Arrange
        String script = "return redis.call('GET', KEYS[1])";
        List<String> keys = List.of("test-key");
        String expectedResult = "test-value";

        // Use a more generic matcher to avoid unnecessary stubbing issues
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(expectedResult);

        // Act
        String result = scriptExecutor.executeScript(script, String.class, keys, "arg1", "arg2");

        // Assert
        assertThat(result).isEqualTo(expectedResult);

        // Verify script execution
        verify(redisTemplate).execute(scriptCaptor.capture(), eq(keys), eq("arg1"), eq("arg2"));

        // Verify the script content
        RedisScript<Object> capturedScript = scriptCaptor.getValue();
        assertThat(capturedScript.getScriptAsString()).isEqualTo(script);
        assertThat(capturedScript.getResultType()).isEqualTo(String.class);
    }

    @Test
    void executeScript_withoutReturnTypeClass_shouldExecuteRedisScript() {
        // Arrange
        String script = "return 42";
        List<String> keys = List.of("test-key");
        Integer expectedResult = 42;

        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(expectedResult);

        // Act
        Object result = scriptExecutor.executeScript(script, keys);

        // Assert
        assertThat(result).isEqualTo(expectedResult);

        // Verify script execution
        verify(redisTemplate).execute(scriptCaptor.capture(), eq(keys));

        // Verify script content
        assertThat(scriptCaptor.getValue().getScriptAsString()).isEqualTo(script);
    }

    @Test
    void incrementWithinRange_shouldExecuteCorrespondingScript() {
        // Arrange
        String key = "counter-key";
        long min = 1;
        long max = 10;
        Long expectedResult = 5L;

        // Use more general matching to avoid unnecessary stubbing issues
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(expectedResult);

        // Act
        Long result = scriptExecutor.incrementWithinRange(key, min, max);

        // Assert
        assertThat(result).isEqualTo(expectedResult);

        // Verify script execution with the right parameters
        verify(redisTemplate).execute(any(RedisScript.class), eq(List.of(key)), eq(min), eq(max));
    }

    @Test
    void setIfNotExists_shouldExecuteCorrespondingScript() {
        // Arrange
        String key = "lock-key";
        String value = "lock-value";
        long ttl = 30;

        // Use consistent argument matchers (eq() for all parameters)
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of(key)), eq(value), eq(ttl)))
                .thenReturn(true);

        // Act
        Boolean result = scriptExecutor.setIfNotExists(key, value, ttl);

        // Assert
        assertThat(result).isTrue();

        // Verify script execution
        verify(redisTemplate).execute(any(RedisScript.class), eq(List.of(key)), eq(value), eq(ttl));
    }
}
