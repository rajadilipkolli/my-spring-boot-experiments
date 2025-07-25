package com.example.featuretoggle;

import com.example.featuretoggle.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestTogglzApplication {

    public static void main(String[] args) {
        SpringApplication.from(TogglzApplication::main)
                .with(ContainerConfig.class)
                .run(args);
    }
}
