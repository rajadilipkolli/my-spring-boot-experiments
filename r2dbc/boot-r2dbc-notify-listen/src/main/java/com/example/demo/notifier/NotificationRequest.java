package com.example.demo.notifier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NotificationRequest(
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_]+") String channel, @NotBlank String message) {}
