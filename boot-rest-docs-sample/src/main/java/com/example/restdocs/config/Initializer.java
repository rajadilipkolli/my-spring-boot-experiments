package com.example.restdocs.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class Initializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(Initializer.class);
    private final ApplicationProperties properties;

    public Initializer(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
    }
}
