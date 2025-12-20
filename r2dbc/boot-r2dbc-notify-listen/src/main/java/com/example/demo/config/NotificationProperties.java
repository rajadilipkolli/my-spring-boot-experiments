package com.example.demo.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.notification")
@Validated
/*
 * Configuration properties for PostgreSQL notification listener.
 *
 * @param channelName Name of the notification channel to listen to
 * @param listenerEnabled Flag to enable/disable the notification listener
 * @param connectionPoolSize Size of the R2DBC connection pool (1-100)
 */
public record NotificationProperties(
        @NotBlank String channelName,
        @NotNull Boolean listenerEnabled,
        @Min(1) @Max(100) Integer connectionPoolSize) {}
