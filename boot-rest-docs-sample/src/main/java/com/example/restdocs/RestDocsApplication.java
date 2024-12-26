package com.example.restdocs;

import com.example.restdocs.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class RestDocsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestDocsApplication.class, args);
    }
}
