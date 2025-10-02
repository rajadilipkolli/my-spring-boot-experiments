package com.example.multitenancy.config;

import com.example.multitenancy.config.multitenant.MultiTenantInterceptor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
class WebMvcConfig implements WebMvcConfigurer {

    private final ApplicationProperties properties;
    private final MultiTenantInterceptor multiTenantInterceptor;

    WebMvcConfig(ApplicationProperties properties, MultiTenantInterceptor multiTenantInterceptor) {
        this.properties = properties;
        this.multiTenantInterceptor = multiTenantInterceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(multiTenantInterceptor);
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping(properties.getCors().getPathPattern())
                .allowedMethods(properties.getCors().getAllowedMethods())
                .allowedHeaders(properties.getCors().getAllowedHeaders())
                .allowedOriginPatterns(properties.getCors().getAllowedOriginPatterns())
                .allowCredentials(properties.getCors().isAllowCredentials());
    }
}
