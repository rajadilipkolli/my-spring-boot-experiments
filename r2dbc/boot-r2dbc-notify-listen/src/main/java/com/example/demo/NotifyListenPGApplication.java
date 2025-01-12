package com.example.demo;

import com.example.demo.config.NotificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NotificationProperties.class)
public class NotifyListenPGApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifyListenPGApplication.class, args);
    }
}
