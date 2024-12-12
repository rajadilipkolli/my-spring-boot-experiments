package com.example.archunit;

import com.example.archunit.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class ArchUnitApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchUnitApplication.class, args);
    }
}
