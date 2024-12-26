package com.example.choasmonkey;

import com.example.choasmonkey.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class ChoasMonkeyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChoasMonkeyApplication.class, args);
    }
}
