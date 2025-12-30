package com.example.keysetpagination;

import com.example.keysetpagination.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestWindowPaginationApplication {

    static void main(String[] args) {
        SpringApplication.from(WindowPaginationApplication::main)
                .withAdditionalProfiles("local")
                .with(ContainersConfig.class)
                .run(args);
    }
}
