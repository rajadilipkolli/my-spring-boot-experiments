package com.example.archunit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

    private final ApplicationProperties properties;

    // Explicit constructor for dependency injection
    public Initializer(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        LOGGER.info("Running Initializer.....");
    }
}
