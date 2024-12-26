package com.example.graphql;

import com.example.graphql.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestGraphQLWebFluxApplication {

    public static void main(String[] args) {
        SpringApplication.from(GraphQLWebFluxApplication::main)
                .with(ContainerConfig.class)
                .run(args);
    }
}
