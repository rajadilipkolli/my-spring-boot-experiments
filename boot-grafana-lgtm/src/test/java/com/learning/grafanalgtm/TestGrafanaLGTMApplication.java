package com.learning.grafanalgtm;

import java.time.Duration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestGrafanaLGTMApplication {

    public static void main(String[] args) {
        SpringApplication.from(GrafanaLGTMApplication::main)
                .with(TestGrafanaLGTMApplication.class)
                .run(args);
    }

    @Bean
    @ServiceConnection("otel/opentelemetry-collector-contrib")
    LgtmStackContainer lgtmContainer() {
        return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.6.0"))
                .withStartupTimeout(Duration.ofMinutes(2));
    }
}
