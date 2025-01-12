package com.example.demo.notifier;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(@NotBlank String channel, @NotBlank String message) {}
