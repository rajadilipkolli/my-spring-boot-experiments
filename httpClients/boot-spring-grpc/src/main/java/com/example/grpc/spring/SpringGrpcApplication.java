package com.example.grpc.spring;

import com.example.grpc.spring.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class SpringGrpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringGrpcApplication.class, args);
    }
}
