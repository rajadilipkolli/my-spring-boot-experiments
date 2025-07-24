package com.example.opensearch;

import com.example.opensearch.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

class TestOpenSearchApplication {

    public static void main(String[] args) {
        SpringApplication.from(OpenSearchApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
