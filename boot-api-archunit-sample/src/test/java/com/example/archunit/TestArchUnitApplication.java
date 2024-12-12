package com.example.archunit;

import com.example.archunit.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestArchUnitApplication {

    public static void main(String[] args) {
        SpringApplication.from(ArchUnitApplication::main)
                .with(ContainersConfig.class)
                .withAdditionalProfiles("local")
                .run(args);
    }
}
