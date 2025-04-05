package com.example.jooq.r2dbc.config;

import org.springframework.boot.autoconfigure.r2dbc.ProxyConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
@EnableR2dbcAuditing(auditorAwareRef = "myAuditorProvider")
public class R2dbcConfiguration {

    @Bean
    ReactiveAuditorAware<String> myAuditorProvider() {
        return () -> Mono.justOrEmpty("appUser");
    }

    @Bean
    ProxyConnectionFactoryCustomizer proxyConnectionFactoryCustomizer(
            QueryProxyExecutionListener queryProxyExecutionListener) {
        return connectionFactory -> connectionFactory.listener(queryProxyExecutionListener);
    }
}
