package com.example.ultimateredis.monitor;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisStatusMonitor {
    private static final Logger log = LoggerFactory.getLogger(RedisStatusMonitor.class);

    private final RedisConnectionFactory redisConnectionFactory;
    private final Environment environment;

    @Value("${spring.data.redis.sentinel.master:mymaster}")
    private String sentinelMaster;

    public RedisStatusMonitor(RedisConnectionFactory redisConnectionFactory, Environment environment) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.environment = environment;
    }

    /**
     * Scheduled task that runs every 30 seconds to check Redis connection status Logs any issues
     * encountered
     */
    @Scheduled(fixedRateString = "${redis.monitor.interval:30000}")
    public void monitorRedisStatus() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            // Check if ping works - do this first since it's simpler
            String pingResult = connection.ping();
            if (!"PONG".equalsIgnoreCase(pingResult)) {
                log.warn("Redis ping returned unexpected response: {}", pingResult);
            }

            // Get server info
            Properties info = connection.serverCommands().info();

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

            if (Arrays.asList(environment.getActiveProfiles()).contains("cluster")) {
                try {
                    RedisClusterConnection clusterConnection = redisConnectionFactory.getClusterConnection();
                    long nodesCount = StreamSupport.stream(
                                    clusterConnection.clusterGetNodes().spliterator(), false)
                            .count();
                    log.info("Redis Cluster Status: nodes={}, slots={}", nodesCount, "OK (simulated slot check)");
                } catch (Exception ex) {
                    log.warn("Failed to retrieve cluster status: {}", ex.getMessage());
                }
            } else if (Arrays.asList(environment.getActiveProfiles()).contains("sentinel")) {
                try {
                    RedisSentinelConnection sentinelConnection = redisConnectionFactory.getSentinelConnection();
                    NamedNode masterNode = () -> sentinelMaster;
                    long replicasCount = sentinelConnection.replicas(masterNode).size();
                    log.info("Redis Sentinel Status: master={}, replicas={}", sentinelMaster, replicasCount);
                } catch (Exception ex) {
                    log.warn("Failed to retrieve sentinel status: {}", ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error monitoring Redis status", e);
        }
    }
}
