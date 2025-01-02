package com.example.keysetpagination;

import com.example.keysetpagination.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestBlazePersistenceApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "local");
        SpringApplication.from(BlazePersistenceApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
