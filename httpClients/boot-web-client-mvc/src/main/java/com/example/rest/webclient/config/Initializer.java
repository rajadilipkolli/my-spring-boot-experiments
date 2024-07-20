package com.example.rest.webclient.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class Initializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
    }
}
