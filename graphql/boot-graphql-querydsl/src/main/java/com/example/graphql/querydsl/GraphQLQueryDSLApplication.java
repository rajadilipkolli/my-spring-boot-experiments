package com.example.graphql.querydsl;

import com.example.graphql.querydsl.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class GraphQLQueryDSLApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphQLQueryDSLApplication.class, args);
    }
}
