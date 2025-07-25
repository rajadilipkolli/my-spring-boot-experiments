package com.example.choasmonkey;

import com.example.choasmonkey.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestChaosMonkeyApplication {

    public static void main(String[] args) {
        SpringApplication.from(ChaosMonkeyApplication::main)
                .with(ContainerConfig.class)
                .run(args);
    }
}
