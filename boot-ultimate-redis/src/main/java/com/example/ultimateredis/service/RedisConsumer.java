package com.example.ultimateredis.service;

import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.annotation.RedisListener;
import org.springframework.stereotype.Service;

@Service
public class RedisConsumer {
    private static final Logger log = LoggerFactory.getLogger(RedisConsumer.class);

    private final AtomicReference<String> lastReceivedMessage = new AtomicReference<>();

    @RedisListener(topic = "app-events")
    public void handleMessage(String message) {
        log.info("Received message from channel 'app-events': {}", message);
        lastReceivedMessage.set(message);
    }

    public String getLastReceivedMessage() {
        return lastReceivedMessage.get();
    }

    public void clear() {
        lastReceivedMessage.set(null);
    }
}
