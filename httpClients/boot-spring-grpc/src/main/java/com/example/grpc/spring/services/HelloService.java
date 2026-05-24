package com.example.grpc.spring.services;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String buildGreeting(String name) {
        String effectiveName = name == null || name.isBlank() ? "World" : name.trim();
        return String.format("Hello %s!", effectiveName);
    }
}
