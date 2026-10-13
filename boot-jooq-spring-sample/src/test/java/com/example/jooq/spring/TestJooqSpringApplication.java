package com.example.jooq.spring;

import com.example.jooq.spring.common.SQLContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestJooqSpringApplication {

    public static void main(String[] args) {
        SpringApplication.from(JooqSpringApplication::main)
                .with(SQLContainerConfig.class)
                .run(args);
    }
}
