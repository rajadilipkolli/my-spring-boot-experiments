package com.example.ultimateredis;

import com.example.ultimateredis.common.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

class TestUltimateRedisApplication {

    public static void main(String[] args) {
        SpringApplication.from(UltimateRedisApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
