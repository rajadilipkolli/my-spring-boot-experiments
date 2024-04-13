package com.learning.grafanalgtm;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrafanaLGTMApplication {

    private static final Logger log = LoggerFactory.getLogger(GrafanaLGTMApplication.class);

    public static void main(String[] args) {
        final Runtime r = Runtime.getRuntime();

        log.info("[APP] Active processors: {}", r.availableProcessors());
        log.info("[APP] Total memory: {}", r.totalMemory());
        log.info("[APP] Free memory: {}", r.freeMemory());
        log.info("[APP] Max memory: {}", r.maxMemory());
        SpringApplication.run(GrafanaLGTMApplication.class, args);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }
}
