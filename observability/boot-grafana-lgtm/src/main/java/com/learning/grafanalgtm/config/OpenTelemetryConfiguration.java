package com.learning.grafanalgtm.config;

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmClassLoadingMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmCpuMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmMemoryMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmThreadMeterConventions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.OpenTelemetryServerRequestObservationConvention;

// Configure OpenTelemetry Semantic Conventions
@Configuration(proxyBeanMethods = false)
public class OpenTelemetryConfiguration {

    @Bean
    OpenTelemetryServerRequestObservationConvention openTelemetryServerRequestObservationConvention() {
        return new OpenTelemetryServerRequestObservationConvention();
    }

    @Bean
    OpenTelemetryJvmCpuMeterConventions openTelemetryJvmCpuMeterConventions() {
        return new OpenTelemetryJvmCpuMeterConventions(Tags.empty());
    }

    @Bean
    OpenTelemetryJvmMemoryMeterConventions openTelemetryJvmMemoryMeterConventions() {
        return new OpenTelemetryJvmMemoryMeterConventions(Tags.empty());
    }

    @Bean
    OpenTelemetryJvmThreadMeterConventions openTelemetryJvmThreadMeterConventions() {
        return new OpenTelemetryJvmThreadMeterConventions(Tags.empty());
    }

    @Bean
    OpenTelemetryJvmClassLoadingMeterConventions openTelemetryJvmClassLoadingMeterConventions() {
        return new OpenTelemetryJvmClassLoadingMeterConventions();
    }
}
