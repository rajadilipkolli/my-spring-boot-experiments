package com.example.ultimateredis;

import com.example.ultimateredis.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ApplicationProperties.class})
public class UltimateRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(UltimateRedisApplication.class, args);
    }
}
