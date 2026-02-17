package com.example.highrps.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app.kafka")
@Configuration
public class AppProperties {

    private Long publishTimeOutMs = 5000L;
    private boolean kafkaStreamsEnabled = true;

    public Long getPublishTimeOutMs() {
        return publishTimeOutMs;
    }

    public void setPublishTimeOutMs(Long publishTimeOutMs) {
        this.publishTimeOutMs = publishTimeOutMs;
    }

    public boolean isKafkaStreamsEnabled() {
        return kafkaStreamsEnabled;
    }

    public void setKafkaStreamsEnabled(boolean kafkaStreamsEnabled) {
        this.kafkaStreamsEnabled = kafkaStreamsEnabled;
    }
}
