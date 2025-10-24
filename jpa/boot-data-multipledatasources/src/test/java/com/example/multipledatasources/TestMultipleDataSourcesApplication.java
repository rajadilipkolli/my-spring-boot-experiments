package com.example.multipledatasources;

import com.example.multipledatasources.common.ContainersConfiguration;
import org.springframework.boot.SpringApplication;

class TestMultipleDataSourcesApplication {

    static void main(String[] args) {
        SpringApplication.from(MultipleDataSourcesApplication::main)
                .with(ContainersConfiguration.class)
                .run(args);
    }
}
