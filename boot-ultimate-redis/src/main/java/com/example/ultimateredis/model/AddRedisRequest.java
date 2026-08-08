package com.example.ultimateredis.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AddRedisRequest(
        @NotBlank String key,
        @NotBlank String value,
        @Positive Integer expireMinutes) {}
