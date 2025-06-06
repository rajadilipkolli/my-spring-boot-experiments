package com.example.ultimateredis.monitor;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisStatusMonitor {
    private static final Logger log = LoggerFactory.getLogger(RedisStatusMonitor.class);

    private final RedisConnectionFactory redisConnectionFactory;

    @Value("${spring.data.redis.sentinel.master:mymaster}")
    private String sentinelMaster;

    public RedisStatusMonitor(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    /**
     * Scheduled task that runs every 30 seconds to check Redis connection status Logs any issues
     * encountered
     */
    @Scheduled(fixedRate = 30000)
    public void monitorRedisStatus() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            // Check if ping works - do this first since it's simpler
            String pingResult = connection.ping();
            if (!"PONG".equalsIgnoreCase(pingResult)) {
                log.warn("Redis ping returned unexpected response: {}", pingResult);
            }

            // Get server info
            Properties info = connection.info();

            String redisVersion = info.getProperty("redis_version");
            String uptime = info.getProperty("uptime_in_seconds");
            String connectedClients = info.getProperty("connected_clients");
            String usedMemory = info.getProperty("used_memory_human");

            log.info(
                    "Redis Status - Version: {}, Uptime: {} seconds, Clients: {}, Memory: {}",
                    redisVersion,
                    uptime,
                    connectedClients,
                    usedMemory);
        } catch (Exception e) {
            log.error("Error monitoring Redis status", e);
        }
    }
}
