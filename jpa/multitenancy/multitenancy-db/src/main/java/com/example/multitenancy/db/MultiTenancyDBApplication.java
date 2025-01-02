package com.example.multitenancy.db;

import com.example.multitenancy.db.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class MultiTenancyDBApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiTenancyDBApplication.class, args);
    }
}
