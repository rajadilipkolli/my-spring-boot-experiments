package com.learning.shedlock;

import com.learning.shedlock.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class ShedLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShedLockApplication.class, args);
    }
}
