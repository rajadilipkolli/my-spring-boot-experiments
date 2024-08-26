package com.example.graphql.querydsl;

import com.example.graphql.querydsl.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestGraphQLQueryDSLApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "local");
        SpringApplication.from(GraphQLQueryDSLApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
