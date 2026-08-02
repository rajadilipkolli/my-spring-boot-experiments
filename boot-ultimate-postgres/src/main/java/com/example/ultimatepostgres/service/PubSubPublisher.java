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
        jdbcTemplate.queryForObject("SELECT pg_notify(?, ?)", String.class, CHANNEL, message);
    }
}
