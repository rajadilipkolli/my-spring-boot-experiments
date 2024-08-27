package com.example.jobrunr;

import com.example.jobrunr.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class JobRunrApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobRunrApplication.class, args);
    }
}
