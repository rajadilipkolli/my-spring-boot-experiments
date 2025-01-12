package com.example.demo.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.r2dbc.proxy.ProxyConnectionFactoryProvider;
import io.r2dbc.spi.ConnectionFactoryOptions;
import java.time.Duration;
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryOptionsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ReactiveDataConfig {

    private final MeterRegistry registry;

    public ReactiveDataConfig(MeterRegistry registry) {
        this.registry = registry;
    }

    @Bean
    ConnectionFactoryOptionsBuilderCustomizer postgresCustomizer() {
        return builder -> {
            builder.option(ConnectionFactoryOptions.LOCK_WAIT_TIMEOUT, Duration.ofSeconds(30));
            builder.option(ConnectionFactoryOptions.STATEMENT_TIMEOUT, Duration.ofMinutes(1));
            builder.option(ConnectionFactoryOptions.DRIVER, "proxy");
            builder.option(ConnectionFactoryOptions.PROTOCOL, "postgresql");
            builder.option(
                    ProxyConnectionFactoryProvider.PROXY_LISTENERS,
                    new MetricsExecutionListener(registry, Duration.ofSeconds(5)));
        };
    }
}
