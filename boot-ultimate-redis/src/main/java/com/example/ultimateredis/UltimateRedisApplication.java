package com.example.ultimateredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class UltimateRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(UltimateRedisApplication.class, args);
    }
}
