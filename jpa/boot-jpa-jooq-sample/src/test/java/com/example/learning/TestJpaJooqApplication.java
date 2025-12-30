package com.example.learning;

import com.example.learning.common.SQLContainerConfig;
import org.springframework.boot.SpringApplication;

class TestJpaJooqApplication {

    static void main(String[] args) {
        SpringApplication.from(JpaJooqApplication::main)
                .with(SQLContainerConfig.class)
                .run(args);
    }
}
