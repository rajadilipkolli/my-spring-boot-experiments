package com.example.ultimateredis.model;

public record AddRedisRequest(String key, String value, Integer expireMinutes) {}
