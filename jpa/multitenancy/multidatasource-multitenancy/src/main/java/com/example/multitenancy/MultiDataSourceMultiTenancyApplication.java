package com.example.multitenancy;

import com.example.multitenancy.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class MultiDataSourceMultiTenancyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiDataSourceMultiTenancyApplication.class, args);
    }
}
