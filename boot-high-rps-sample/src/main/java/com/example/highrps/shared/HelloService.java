package com.example.highrps.shared;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String ping() {
        return "pong";
    }
}
