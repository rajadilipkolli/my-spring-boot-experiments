package com.example.mongoes;

import com.example.mongoes.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class MongoESApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongoESApplication.class, args);
    }
}
