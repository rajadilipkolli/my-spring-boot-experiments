package com.example.multitenancy.schema;

import com.example.multitenancy.schema.config.TestContainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestMultitenancySchemaApplication {

    public static void main(String[] args) {
        SpringApplication.from(MultitenancySchemaApplication::main)
                .with(TestContainersConfiguration.class)
                .run(args);
    }
}
