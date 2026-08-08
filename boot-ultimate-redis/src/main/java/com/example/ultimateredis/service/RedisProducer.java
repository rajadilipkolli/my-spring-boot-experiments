package com.example.ultimateredis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisProducer {
    private static final Logger log = LoggerFactory.getLogger(RedisProducer.class);
    private final StringRedisTemplate stringRedisTemplate;

    public RedisProducer(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void publishMessage(String topic, String message) {
        log.info("Publishing message to topic {}", topic);
        stringRedisTemplate.convertAndSend(topic, message);
    }
}
