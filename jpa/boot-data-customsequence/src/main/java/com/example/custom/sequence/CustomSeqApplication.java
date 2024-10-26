package com.example.custom.sequence;

import com.example.custom.sequence.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class CustomSeqApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomSeqApplication.class, args);
    }
}
