package com.example.graphql;

import com.example.graphql.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class GraphQLWebFluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphQLWebFluxApplication.class, args);
    }
}
