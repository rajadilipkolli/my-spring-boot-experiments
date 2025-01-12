package com.example.multitenancy.partition;

import com.example.multitenancy.partition.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class MultiTenancyPartitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiTenancyPartitionApplication.class, args);
    }
}
