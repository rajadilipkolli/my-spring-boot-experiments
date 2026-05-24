package com.example.grpc.spring.web.controllers;

import com.example.grpc.spring.model.GreetingResponse;
import com.example.grpc.spring.services.HelloService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping("/api/hello")
    public ResponseEntity<GreetingResponse> hello(@RequestParam(required = false) String name) {
        String message = helloService.buildGreeting(name);
        return ResponseEntity.ok(new GreetingResponse(message));
    }
}
