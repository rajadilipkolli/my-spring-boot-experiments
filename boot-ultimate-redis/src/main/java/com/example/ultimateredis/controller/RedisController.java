package com.example.ultimateredis.controller;

import com.example.ultimateredis.model.AddRedisRequest;
import com.example.ultimateredis.model.GenericResponse;
import com.example.ultimateredis.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/redis")
public class RedisController {

    private final RedisService redisService;

    public RedisController(RedisService redisService) {
        this.redisService = redisService;
    }

    @PostMapping("/add")
    public ResponseEntity<GenericResponse<Boolean>> addRedisKeyValue(@RequestBody AddRedisRequest redisRequest) {

        redisService.addRedis(redisRequest);
        return new ResponseEntity<>(new GenericResponse<>(Boolean.TRUE), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<GenericResponse<String>> getFromCache(@RequestParam String key) {

        String value = redisService.getValue(key);
        return ResponseEntity.ok(new GenericResponse<>(value));
    }

    @Operation(
            summary = "Get keys by pattern",
            description =
                    "Retrieve Redis keys matching the specified pattern. Use with caution as patterns like '*' can impact performance.")
    @Parameter(name = "pattern", description = "Redis key pattern (e.g., 'user:*', 'cache:session:*')")
    @GetMapping("/keys")
    public ResponseEntity<GenericResponse<Set<String>>> getKeysByPattern(@RequestParam String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty");
        }
        Set<String> keys = redisService.getKeysByPattern(pattern);
        return ResponseEntity.ok(new GenericResponse<>(keys));
    }

    @DeleteMapping("/keys")
    public ResponseEntity<GenericResponse<Boolean>> deleteKeysByPattern(@RequestParam String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty");
        }
        if ("*".equals(pattern.trim())) {
            throw new IllegalArgumentException("Deleting all keys is not allowed for safety reasons");
        }
        redisService.deleteByPattern(pattern);
        return ResponseEntity.ok(new GenericResponse<>(Boolean.TRUE));
    }
}
