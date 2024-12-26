package com.example.locks;

import com.example.locks.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class JpaLocksApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaLocksApplication.class, args);
    }
}
