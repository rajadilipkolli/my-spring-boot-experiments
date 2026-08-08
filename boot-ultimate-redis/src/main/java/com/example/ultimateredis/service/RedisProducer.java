package com.example.ultimateredis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisProducer {
    private static final Logger log = LoggerFactory.getLogger(RedisProducer.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisProducer(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publishMessage(String topic, String message) {
        log.info("Publishing message to topic {}: {}", topic, message);
        redisTemplate.convertAndSend(topic, message);
    }
}
