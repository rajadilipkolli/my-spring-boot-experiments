package com.example.highrps.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app.kafka")
@Configuration
public class AppProperties {

    private Long publishTimeOutMs = 5000L;

    public Long getPublishTimeOutMs() {
        return publishTimeOutMs;
    }

    public void setPublishTimeOutMs(Long publishTimeOutMs) {
        this.publishTimeOutMs = publishTimeOutMs;
    }
}
