package com.example.jndi;

import com.example.jndi.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class JNDIApplication {

    public static void main(String[] args) {
        SpringApplication.run(JNDIApplication.class, args);
    }
}
