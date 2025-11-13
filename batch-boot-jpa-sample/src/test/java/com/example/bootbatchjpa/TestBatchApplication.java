package com.example.bootbatchjpa;

import com.example.bootbatchjpa.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestBatchApplication {

    static void main(String[] args) {
        SpringApplication.from(BatchApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
