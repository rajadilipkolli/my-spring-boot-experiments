package com.example.ultimatepostgres.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PubSubPublisher {

    private final JdbcTemplate jdbcTemplate;
    private static final String CHANNEL = "ultimate_channel";

    public PubSubPublisher(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void publish(String message) {
        if (message != null && message.getBytes(java.nio.charset.StandardCharsets.UTF_8).length >= 8000) {
            throw new IllegalArgumentException("Payload size must be less than 8000 bytes");
        }
        jdbcTemplate.queryForObject("SELECT pg_notify(?, ?)", String.class, CHANNEL, message);
    }
}
