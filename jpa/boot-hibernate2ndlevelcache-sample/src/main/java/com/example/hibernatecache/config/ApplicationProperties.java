package com.example.hibernatecache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("application")
public record ApplicationProperties(@DefaultValue Cors cors) {

    public ApplicationProperties {
        if (cors == null) {
            cors = new Cors("/api/**", "*", "*", "*", true);
        }
    }

    public record Cors(
            @DefaultValue("/api/**") String pathPattern,
            @DefaultValue("*") String allowedMethods,
            @DefaultValue("*") String allowedHeaders,
            @DefaultValue("*") String allowedOriginPatterns,
            @DefaultValue("true") boolean allowCredentials) {}
}
