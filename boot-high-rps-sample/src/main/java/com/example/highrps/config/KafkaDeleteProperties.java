package com.example.highrps.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.delete")
public class KafkaDeleteProperties {

    /** Total wait time in milliseconds to wait for the posts-store to reflect deletion. */
    private long waitTimeMs = 5000;

    /** Poll interval in milliseconds to check the posts-store. */
    private long pollIntervalMs = 200L;

    public long getWaitTimeMs() {
        return waitTimeMs;
    }

    public void setWaitTimeMs(long waitTimeMs) {
        this.waitTimeMs = waitTimeMs;
    }

    public long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }
}
