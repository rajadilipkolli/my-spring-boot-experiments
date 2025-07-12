package com.example.restdocs;

import com.example.restdocs.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestRestDocsApplication {

    public static void main(String[] args) {
        SpringApplication.from(RestDocsApplication::main)
                .with(ContainerConfig.class)
                .run(args);
    }
}
