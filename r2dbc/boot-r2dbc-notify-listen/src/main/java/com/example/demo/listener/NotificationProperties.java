package com.example.demo.listener;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.notification")
@Validated
public record NotificationProperties(
        @NotBlank String channelName, @NotNull Boolean listenerEnabled, @Min(1) @Max(100) Integer connectionPoolSize) {}
