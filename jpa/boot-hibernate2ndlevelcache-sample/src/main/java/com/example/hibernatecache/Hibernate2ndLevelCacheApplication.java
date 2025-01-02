package com.example.hibernatecache;

import com.example.hibernatecache.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class Hibernate2ndLevelCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(Hibernate2ndLevelCacheApplication.class, args);
    }
}
