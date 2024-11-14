package com.example.graphql;

import com.example.graphql.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class GraphQlWebMvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphQlWebMvcApplication.class, args);
    }
}
