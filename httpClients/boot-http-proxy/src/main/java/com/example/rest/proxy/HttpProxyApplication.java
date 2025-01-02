package com.example.rest.proxy;

import com.example.rest.proxy.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class HttpProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HttpProxyApplication.class, args);
    }
}
