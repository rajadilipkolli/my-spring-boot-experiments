package com.example.envers;

import com.example.envers.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class JpaEnversApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaEnversApplication.class, args);
    }
}
