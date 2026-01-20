package com.example.multitenancy.schema.config;

import java.util.Arrays;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final ApplicationProperties properties;

    public WebMvcConfig(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping(properties.getCors().getPathPattern())
                .allowedMethods(splitAndTrim(properties.getCors().getAllowedMethods()))
                .allowedHeaders(splitAndTrim(properties.getCors().getAllowedHeaders()))
                .allowedOriginPatterns(splitAndTrim(properties.getCors().getAllowedOriginPatterns()))
                .allowCredentials(properties.getCors().isAllowCredentials());
    }

    private static String[] splitAndTrim(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}
