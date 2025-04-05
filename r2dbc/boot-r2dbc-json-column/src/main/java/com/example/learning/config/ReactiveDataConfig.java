package com.example.learning.config;

import brave.Tracer;
import io.micrometer.core.instrument.MeterRegistry;
import io.r2dbc.proxy.ProxyConnectionFactoryProvider;
import io.r2dbc.spi.ConnectionFactoryOptions;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryOptionsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
@EnableR2dbcAuditing
public class ReactiveDataConfig {

    private final MeterRegistry registry;
    private final Tracer tracer;

    public ReactiveDataConfig(MeterRegistry registry, Tracer tracer) {
        this.registry = registry;
        this.tracer = tracer;
    }

    @Bean
    ConnectionFactoryOptionsBuilderCustomizer postgresCustomizer() {
        return builder -> {
            builder.option(ConnectionFactoryOptions.LOCK_WAIT_TIMEOUT, Duration.ofSeconds(30))
                    .option(ConnectionFactoryOptions.STATEMENT_TIMEOUT, Duration.ofMinutes(1))
                    .option(ConnectionFactoryOptions.DRIVER, "proxy")
                    .option(ConnectionFactoryOptions.PROTOCOL, "postgresql")
                    .option(
                            ProxyConnectionFactoryProvider.PROXY_LISTENERS,
                            List.of(
                                    new MetricsExecutionListener(registry, Duration.ofSeconds(5)),
                                    new QueryTimeMetricsExecutionListener(registry),
                                    new TracingExecutionListener(tracer)));
        };
    }

    @Bean
    ReactiveAuditorAware<String> auditorAware() {
        return () -> Mono.just("appUser");
    }
}
