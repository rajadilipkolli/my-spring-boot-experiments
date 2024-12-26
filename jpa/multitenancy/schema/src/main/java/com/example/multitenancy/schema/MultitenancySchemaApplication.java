package com.example.multitenancy.schema;

import com.example.multitenancy.schema.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class MultitenancySchemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultitenancySchemaApplication.class, args);
    }
}
