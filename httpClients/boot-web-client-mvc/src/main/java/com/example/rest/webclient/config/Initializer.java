package com.example.rest.webclient.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class Initializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
    }
}
