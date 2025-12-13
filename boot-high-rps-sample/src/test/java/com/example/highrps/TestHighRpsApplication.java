package com.example.highrps;

import com.example.highrps.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

class TestHighRpsApplication {

    public static void main(String[] args) {
        SpringApplication.from(HighRpsApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
