package com.example.jooq.r2dbc.config;

import io.r2dbc.proxy.ProxyConnectionFactoryProvider;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryOptionsBuilderCustomizer;
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
    ConnectionFactoryOptionsBuilderCustomizer connectionFactoryOptionsBuilderCustomizer(
            QueryProxyExecutionListener queryProxyExecutionListener) {
        return builder -> {
            builder.option(ConnectionFactoryOptions.DRIVER, "proxy");
            builder.option(ConnectionFactoryOptions.PROTOCOL, "pool:postgresql");
            builder.option(
                    ProxyConnectionFactoryProvider.PROXY_LISTENERS, queryProxyExecutionListener);
        };
    }
}
