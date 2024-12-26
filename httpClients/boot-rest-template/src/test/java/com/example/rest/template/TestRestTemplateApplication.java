package com.example.rest.template;

import com.example.rest.template.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

class TestRestTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.from(RestTemplateApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
