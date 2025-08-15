package com.example.ultimateredis.monitor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RedisStatusMonitorTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisConnection redisConnection;

    @InjectMocks
    private RedisStatusMonitor redisStatusMonitor;

    @BeforeEach
    void setUp() {
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);

        // Set the sentinelMaster field through reflection
        ReflectionTestUtils.setField(redisStatusMonitor, "sentinelMaster", "mymaster");
    }

    @Test
    void monitorRedisStatus_whenRedisIsUp_shouldLogStatus() {
        // Arrange
        when(redisConnection.ping()).thenReturn("PONG");

        Properties redisInfo = new Properties();
        redisInfo.setProperty("redis_version", "7.0.0");
        redisInfo.setProperty("uptime_in_seconds", "3600");
        redisInfo.setProperty("connected_clients", "10");
        redisInfo.setProperty("used_memory_human", "100M");

        // Mock the serverCommands() to return the same redisConnection (since info() is called on
        // serverCommands())
        when(redisConnection.serverCommands()).thenReturn(redisConnection);
        when(redisConnection.info()).thenReturn(redisInfo);

        // Act
        redisStatusMonitor.monitorRedisStatus();

        // Assert
        verify(redisConnection).ping();
        verify(redisConnection).serverCommands();
        verify(redisConnection).info();
    }

    @Test
    void monitorRedisStatus_whenRedisReturnsUnexpectedPing_shouldLogWarning() {
        // Arrange
        when(redisConnection.ping()).thenReturn("ERROR");
        when(redisConnection.serverCommands()).thenReturn(redisConnection);
        when(redisConnection.info()).thenReturn(new Properties());

        // Act
        redisStatusMonitor.monitorRedisStatus();

        // Assert
        verify(redisConnection).ping();
        verify(redisConnection).serverCommands();
        verify(redisConnection).info();
    }

    @Test
    void monitorRedisStatus_whenExceptionOccurs_shouldLogError() {
        // Arrange
        when(redisConnection.ping()).thenThrow(new RuntimeException("Connection refused"));

        // Act
        redisStatusMonitor.monitorRedisStatus();

        // No assertions needed, we just verify no exceptions bubble up
    }
}
