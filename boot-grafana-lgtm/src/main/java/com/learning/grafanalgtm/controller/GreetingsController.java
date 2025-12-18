package com.learning.grafanalgtm.controller;

import com.learning.grafanalgtm.service.GreetingsService;
import io.micrometer.core.annotation.Counted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreetingsController.class);

    private final GreetingsService greetingsService;

    public GreetingsController(GreetingsService greetingsService) {
        this.greetingsService = greetingsService;
    }

    @Counted
    @GetMapping("/greetings")
    public GenericResponse greetings(@RequestParam(required = false) String username) {
        LOGGER.info("Inside Greetings method");
        return new GenericResponse(greetingsService.greetingByUser(username));
    }

    private record GenericResponse(String message) {}
}
