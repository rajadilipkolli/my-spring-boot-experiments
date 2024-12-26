package com.example.envers;

import com.example.envers.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestJpaEnversApplication {

    public static void main(String[] args) {
        SpringApplication.from(JpaEnversApplication::main)
                .with(ContainersConfig.class)
                .withAdditionalProfiles("local")
                .run(args);
    }
}
