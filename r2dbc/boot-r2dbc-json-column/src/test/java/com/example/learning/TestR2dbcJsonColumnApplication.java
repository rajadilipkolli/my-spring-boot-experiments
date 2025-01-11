package com.example.learning;

import com.example.learning.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestR2dbcJsonColumnApplication {

    public static void main(String[] args) {
        SpringApplication.from(R2dbcJsonColumnApplication::main)
                .with(ContainerConfig.class)
                .run(args);
    }
}
