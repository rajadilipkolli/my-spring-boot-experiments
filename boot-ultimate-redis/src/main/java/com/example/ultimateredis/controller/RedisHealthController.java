package com.example.ultimateredis.controller;

import com.example.ultimateredis.model.GenericResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/redis/health")
public class RedisHealthController {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHealthController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping
    public ResponseEntity<GenericResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> healthStatus = new HashMap<>();

        boolean isConnected;
        String errorMessage = null;

        try {
            // Ping Redis to check connectivity
            String pingResult = redisTemplate.getConnectionFactory().getConnection().ping();
            isConnected = "PONG".equalsIgnoreCase(pingResult);

            // Add connection info if available
            healthStatus.put(
                    "connectionInfo",
                    redisTemplate.getConnectionFactory().getConnection().serverCommands().info());
        } catch (Exception e) {
            isConnected = false;
            errorMessage = e.getMessage();
        }

        healthStatus.put("status", isConnected ? "UP" : "DOWN");
        healthStatus.put("timestamp", LocalDateTime.now().toString());

        if (errorMessage != null) {
            healthStatus.put("error", errorMessage);
        }

        return ResponseEntity.ok(new GenericResponse<>(healthStatus));
    }
}
