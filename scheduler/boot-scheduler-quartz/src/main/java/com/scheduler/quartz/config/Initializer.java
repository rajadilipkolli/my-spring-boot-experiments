package com.scheduler.quartz.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements CommandLineRunner {

    private final ApplicationProperties properties;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
    }
}
