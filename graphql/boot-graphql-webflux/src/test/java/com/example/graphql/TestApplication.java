package com.example.graphql;

import com.example.graphql.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(ContainerConfig.class).run(args);
    }
}
