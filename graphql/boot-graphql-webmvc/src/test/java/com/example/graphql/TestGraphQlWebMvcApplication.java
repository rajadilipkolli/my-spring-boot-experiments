package com.example.graphql;

import com.example.graphql.common.TestContainersConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@TestConfiguration(proxyBeanMethods = false)
@ImportTestcontainers(TestContainersConfig.class)
public class TestGraphQlWebMvcApplication {

    static void main(String[] args) {
        SpringApplication.from(GraphQlWebMvcApplication::main)
                .with(TestGraphQlWebMvcApplication.class)
                .run(args);
    }
}
