package com.example.restclient.bootrestclient;

import com.example.restclient.bootrestclient.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class BootRestClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootRestClientApplication.class, args);
    }
}
