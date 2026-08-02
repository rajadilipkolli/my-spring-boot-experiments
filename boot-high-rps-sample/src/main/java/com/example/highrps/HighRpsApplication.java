package com.example.highrps;

import com.example.highrps.shared.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class HighRpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HighRpsApplication.class, args);
    }
}
