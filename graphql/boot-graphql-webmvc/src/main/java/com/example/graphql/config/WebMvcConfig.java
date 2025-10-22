package com.example.graphql.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
class WebMvcConfig implements WebMvcConfigurer {
    private final ApplicationProperties properties;

    WebMvcConfig(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping(properties.getCors().getPathPattern())
                .allowedMethods(properties.getCors().getAllowedMethods().split(","))
                .allowedHeaders(properties.getCors().getAllowedHeaders().split(","))
                .allowedOriginPatterns(
                        properties.getCors().getAllowedOriginPatterns().split(","))
                .allowCredentials(properties.getCors().isAllowCredentials());
    }
}
