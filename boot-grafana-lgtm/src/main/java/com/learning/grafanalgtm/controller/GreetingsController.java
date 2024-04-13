package com.learning.grafanalgtm.controller;

import io.micrometer.core.annotation.Counted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreetingsController.class);

    @Counted
    @GetMapping("/greetings")
    public String greetings() {
        LOGGER.info("Inside Greetings method");
        return "Hello, World!";
    }
}
