package com.example.highrps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HighRpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HighRpsApplication.class, args);
    }
}
