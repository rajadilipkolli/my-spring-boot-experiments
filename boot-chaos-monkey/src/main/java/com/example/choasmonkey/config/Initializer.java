package com.example.choasmonkey.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements CommandLineRunner {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(Initializer.class);
    private final ApplicationProperties properties;

    public Initializer(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
    }
}
