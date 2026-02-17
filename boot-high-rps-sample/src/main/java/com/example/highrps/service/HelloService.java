package com.example.highrps.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String ping() {
        return "pong";
    }
}
