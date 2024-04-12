package com.learning.grafanalgtm.controller;

import io.micrometer.core.annotation.Counted;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingsController {

    @Counted
    @GetMapping("/greetings")
    public String greetings() {
        return "Hello, World!";
    }

}