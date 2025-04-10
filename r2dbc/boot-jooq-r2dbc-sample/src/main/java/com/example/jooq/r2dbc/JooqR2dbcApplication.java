package com.example.jooq.r2dbc;

import com.example.jooq.r2dbc.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {JooqAutoConfiguration.class})
@EnableConfigurationProperties({ApplicationProperties.class})
public class JooqR2dbcApplication {

    public static void main(String[] args) {
        SpringApplication.run(JooqR2dbcApplication.class, args);
    }
}
