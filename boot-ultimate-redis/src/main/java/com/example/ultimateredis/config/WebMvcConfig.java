package com.example.ultimateredis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
class WebMvcConfig implements WebMvcConfigurer {
    private final ApplicationProperties applicationProperties;
    private final RateLimitInterceptor rateLimitInterceptor;

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    WebMvcConfig(ApplicationProperties applicationProperties, RateLimitInterceptor rateLimitInterceptor) {
        this.applicationProperties = applicationProperties;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        ApplicationProperties.Cors propertiesCors = applicationProperties.getCors();
        registry.addMapping(propertiesCors.getPathPattern())
                .allowedMethods(propertiesCors.getAllowedMethods().split(","))
                .allowedHeaders(propertiesCors.getAllowedHeaders().split(","))
                .allowedOriginPatterns(propertiesCors.getAllowedOriginPatterns().split(","))
                .allowCredentials(propertiesCors.isAllowCredentials());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (rateLimitEnabled) {
            registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**", "/v1/**");
        }
    }
}
