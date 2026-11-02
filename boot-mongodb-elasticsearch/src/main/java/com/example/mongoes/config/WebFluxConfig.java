package com.example.mongoes.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.GeoJacksonModule;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration(proxyBeanMethods = false)
class WebFluxConfig implements WebFluxConfigurer {

    private final ApplicationProperties properties;

    public WebFluxConfig(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping(properties.getCors().getPathPattern())
                .allowedMethods(properties.getCors().getAllowedMethods().split(","))
                .allowedHeaders(properties.getCors().getAllowedHeaders().split(","))
                .allowedOriginPatterns(properties.getCors().getAllowedOriginPatterns().split(","))
                .allowCredentials(properties.getCors().isAllowCredentials());
    }

    @Bean
    GeoJacksonModule geoJacksonModule() {
        return new GeoJacksonModule();
    }
}
