package com.example.jooq.spring;

import com.example.jooq.spring.common.SQLContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestJooqSpringApplication {

    static void main(String[] args) {
        SpringApplication.from(JooqSpringApplication::main)
                .with(SQLContainerConfig.class)
                .withAdditionalProfiles("local")
                .run(args);
    }
}
