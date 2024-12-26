package com.example.keysetpagination;

import com.example.keysetpagination.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class BlazePersistenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlazePersistenceApplication.class, args);
    }
}
